package com.voltiopedal.apiinventario.auth;

import com.voltiopedal.apiinventario.usuario.Usuario;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service 
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /* Lista de señuelos */
    private static final List<String> PREGUNTAS_SENUELO = List.of(
        "¿Nombre de tu primera mascota?",
        "¿Ciudad donde naciste?",
        "¿Nombre de tu colegio de primaria?",
        "¿Comida favorita?"
    );

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicioToken servicioToken;
    private final String dummyHash;

    private final int maxIntentos;
    private final Duration duracionBloqueo;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       ServicioToken servicioToken,
                       @Value("${app.seguridad.intentos-maximos}") int maxIntentos,
                       @Value("${app.seguridad.bloqueo}") Duration duracionBloqueo) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.servicioToken = servicioToken;
        this.maxIntentos = maxIntentos;
        this.duracionBloqueo = duracionBloqueo;
        /* Se genera en el constructor */
        this.dummyHash = passwordEncoder.encode("dummy-password-falso");
    }
    
    @Transactional(noRollbackFor = {CredencialesInvalidasException.class, CuentaBloqueadaException.class})
    public LoginResponse login(LoginRequest solicitud) {
        /* Buscar al usuario */
        var optUsuario = usuarioRepository.findByTipoDocumentoAndNumeroDocumento(solicitud.tipoDocumento(), solicitud.numeroDocumento());

        /* No existe o inactivo: mismo tiempo y mismo mensaje */
        if (optUsuario.isEmpty() || !optUsuario.get().getActivo()) {
            passwordEncoder.matches(solicitud.contrasena(), dummyHash); /* Mismo costo de BCrypt que un usuario real (evita ataques de tiempo) */
            log.warn("Intento de login fallido para documento {} {}", solicitud.tipoDocumento(),solicitud.numeroDocumento());
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
        }

        Usuario usuario = optUsuario.get();
        /* Llamada agregando el 4to parámetro */
        validarSecreto(usuario, solicitud.contrasena(), usuario.getContrasena(), "Usuario o contraseña incorrectos.");

        /* Generar token y retornar */
        var tokenData = servicioToken.crearToken(usuario);
        return new LoginResponse(
            tokenData.token(),
            tokenData.vence(),
            usuario.getNombres(),
            usuario.getApellidos(),
            usuario.getRol()
        );
    }

    /* Metodos nuevos completos */
    @Transactional(readOnly = true)
    public PreguntaResponse obtenerPregunta(PreguntaRequest solicitud) {
        var optUsuario = usuarioRepository.findByTipoDocumentoAndNumeroDocumento(solicitud.tipoDocumento(), solicitud.numeroDocumento());

        /* No existe o inactivo: señuelo determinista (mismo DNI -> misma pregunta) */
        if(optUsuario.isEmpty() || !optUsuario.get().getActivo()) {
            int indice = Math.floorMod(solicitud.numeroDocumento().hashCode(), PREGUNTAS_SENUELO.size());
            return new PreguntaResponse(PREGUNTAS_SENUELO.get(indice));
        }

        return new PreguntaResponse(optUsuario.get().getPreguntaSeguridad());
    }

    @Transactional(noRollbackFor = {CredencialesInvalidasException.class, CuentaBloqueadaException.class})
    public void restablecer(RestablecerRequest solicitud) {
        /* Normalizar igual que al registrar */
        String respuesta = solicitud.respuestaSeguridad().trim().toLowerCase();

        var optUsuario = usuarioRepository.findByTipoDocumentoAndNumeroDocumento(solicitud.tipoDocumento(), solicitud.numeroDocumento());

        /* No existe o inactivo: mismo costo y mismo mensaje */
        if (optUsuario.isEmpty() || !optUsuario.get().getActivo()) {
            passwordEncoder.matches(respuesta, dummyHash);
            log.warn("Intento de recuperación fallido para documento {} {}", solicitud.tipoDocumento(), solicitud.numeroDocumento());
            throw new CredencialesInvalidasException("Documento o respuesta incorrectos.");
        }

        Usuario usuario = optUsuario.get();
        validarSecreto(usuario, respuesta, usuario.getRespuestaSeguridad(), "Documento o respuesta incorrectos.");

        /* Nueva contraseña hasheada (validarSecreto ya reinició el contador) */
        usuario.setContrasena(passwordEncoder.encode(solicitud.nuevaContrasena()));
        log.info("Contraseña restablecida para documento {} {}", usuario.getTipoDocumento(), usuario.getNumeroDocumento());
    }

    private void validarSecreto(Usuario usuario, String secretoPlano, String hashGuardado, String mensajeError) {
        LocalDateTime ahora = LocalDateTime.now();

        /* Bloqueado actualmente -> 429 */
        if (usuario.getBloqueadoHasta() != null && ahora.isBefore(usuario.getBloqueadoHasta())) {
            log.warn("Intento de ingreso a cuenta bloqueada para documento {} {}", usuario.getTipoDocumento(), usuario.getNumeroDocumento());
            throw new CuentaBloqueadaException("Cuenta bloqueada temporalmente.", usuario.getBloqueadoHasta().atZone(ZoneId.systemDefault()).toInstant());
        }

        /* Bloqueado expirado -> reiniciar */
        if (usuario.getBloqueadoHasta() != null && !ahora.isBefore(usuario.getBloqueadoHasta())) {
            usuario.setIntentosFallidos((byte) 0);
            usuario.setBloqueadoHasta(null);
        }
                
        /* Secreto incorrecto -> sumar intento / bloquear / 401 */
        if (!passwordEncoder.matches(secretoPlano, hashGuardado)) {
            byte nuevoIntentos = (byte) (usuario.getIntentosFallidos() + 1);
            usuario.setIntentosFallidos(nuevoIntentos);

            if (nuevoIntentos >= maxIntentos) {
                usuario.setBloqueadoHasta(ahora.plus(duracionBloqueo));
                log.warn("Cuenta alcanzó el limite de intentos y fue bloqueada: {} {}", usuario.getTipoDocumento(), usuario.getNumeroDocumento());
                throw new CuentaBloqueadaException("Cuenta bloqueada temporalmente por demasiados intentos fallidos.", usuario.getBloqueadoHasta().atZone(ZoneId.systemDefault()).toInstant());
            }

            log.warn("Intento fallido ({}/{}) para documento {} {}", nuevoIntentos, maxIntentos, usuario.getTipoDocumento(), usuario.getNumeroDocumento());
            throw new CredencialesInvalidasException(mensajeError);
        }

        /* Acierto -> reiniciar */
        usuario.setIntentosFallidos((byte) 0);
        usuario.setBloqueadoHasta(null);
    }
        
}
