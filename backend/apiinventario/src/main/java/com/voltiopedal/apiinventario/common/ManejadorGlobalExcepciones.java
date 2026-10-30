package com.voltiopedal.apiinventario.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.voltiopedal.apiinventario.auth.CredencialesInvalidasException;
import com.voltiopedal.apiinventario.auth.CuentaBloqueadaException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;
import java.time.Instant;
import java.util.HashMap;
import java.time.Duration;

@RestControllerAdvice 
public class ManejadorGlobalExcepciones {
    /* Instanciar el Logger para esta clase */
    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    /* Manejo de recurso no encontrado (404) */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Recurso no encontrado");
        return problema;
    }

    /* Regla de negocio -> 409 CONFLICT (JSON valido pero el estado choca)*/
    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail manejarReglaNegocio(ReglaNegocioException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setTitle("Regla de negocio violada");
        return problema;
    }

    /* Integridad de la base de datos -> 409 CONFLICT */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail manejarIntegridadDatos(DataIntegrityViolationException ex) {
        /* Se registra como advertencia, pero no se ensucia los errores criticos */
        log.warn("Violación de integridad de datos interceptada: {}", ex.getMessage());

        /* Mensaje limpio y genérico que no expone nombres de tablas ni columnas */
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "La operación no se pudo procesar. Verifique que los datos enviados sean válidos y no estén duplicados");
        problema.setTitle("Conflicto de integridad de datos");
        return problema;
    }
    
    /* LA RED DE SEGURIDAD (500) */
    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarInesperado(Exception ex) {
        /* El detalle técnico asqueroso (incluyendo errores de MySQL) va hacia la consola */
        log.error("Error inesperado en el sistema", ex);

        /* Cliente solo recibe esto: */
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado. Contacte al administrador.");
        problema.setTitle("Error interno del servidor");
        return problema;
    }

    /* Manejo de validaciones de entrada (El JSON tiene mal formato o datos inválidos) -> 400 BAD REQUEST */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        /* Extraer cada campo que falló y su mensaje en español */
        ex.getBindingResult().getFieldErrors().forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Por favor, corrija los errores en el formulario para continuar."
        );
        problema.setTitle("Datos de entrada inválidos");
        problema.setProperty("errores", errores); /* Adjuntar la lista de campos al JSON final */

        return problema;
    }

    /* Manejo de JSON mal formado o ilegible -> 400 BAD REQUEST */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail manejarJsonIlegible(HttpMessageNotReadableException ex) {
        /* Usamos WARN para no contaminar los errores críticos (ERROR) del sistema */
        log.warn("Petición rechazada por JSON mal formado o ilegible: {}", ex.getMessage());

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "El cuerpo de la petición tiene un formato incorrecto. Verifique la sintaxis del JSON");
        problema.setTitle("Petición mal formada");
        return problema;
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ProblemDetail> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Credenciales Inválidas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ProblemDetail> manejarCuentaBloqueada(CuentaBloqueadaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problem.setTitle("Cuenta Bloqueada");

        /* Redondeo seguro hacia arriba para evitar peticiones prematuras */
        long millisRestantes = Duration.between(Instant.now(), ex.getBloqueadoHasta()).toMillis();
        long segundosRestantes = (long) Math.ceil(millisRestantes / 1000.0);
        /* Evita valores negativos si el tiempo pasó justo en ese milisegundo */
        segundosRestantes = Math.max(1, segundosRestantes);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(segundosRestantes))
                .body(problem);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> manejarNoResourceFound(NoResourceFoundException ex) {
        log.warn("Ruta no encontrada: {}", ex.getResourcePath());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "La ruta solicitada no existe o fue escrita incorrectamente.");
        problem.setTitle("Recurso No Encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }
}
