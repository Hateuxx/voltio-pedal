package com.voltiopedal.apiinventario.servicio;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioTecnicoController {
    private final ServicioTecnicoService servicioService;

    public ServicioTecnicoController(ServicioTecnicoService servicioService) {
        this.servicioService = servicioService;
    }

    /* POST /api/servicios: recepción del scooter (RF10) */
    @PostMapping
    public ResponseEntity<ServicioResponse> registrar(@Valid @RequestBody ServicioRequests.Registrar request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.registrar(request));
    }

    /* GET /api/servicios?estado=EN_REPARACION (estado opcional) */
    @GetMapping
    public List<ServicioResponse> listar(@RequestParam(required = false) EstadoServicio estado) {
        return servicioService.listar(estado);
    }

    /* GET /api/servicios/tecnicos: técnicos activos para asignar */
    @GetMapping("/tecnicos")
    public List<ServicioResponse.Tecnico> tecnicos() {
        return servicioService.listarTecnicos();
    }

    @GetMapping("/{id}")
    public ServicioResponse buscarPorId(@PathVariable Integer id) {
        return servicioService.buscarPorId(id);
    }

    /* PATCH /api/servicios/{id}: estado, diagnóstico y técnico */
    @PatchMapping("/{id}")
    public ServicioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ServicioRequests.Actualizar request) {
        return servicioService.actualizar(id, request);
    }

    /* POST /api/servicios/{id}/repuestos: consumo de repuesto (RF17) */
    @PostMapping("/{id}/repuestos")
    public ServicioResponse consumirRepuesto(@PathVariable Integer id, @Valid @RequestBody ServicioRequests.Consumo request,
                                             @AuthenticationPrincipal Jwt jwt) {
        return servicioService.consumirRepuesto(id, request, Integer.valueOf(jwt.getSubject()));
    }

    /* DELETE /api/servicios/{id}/repuestos/{idDetalle}: devuelve el repuesto al almacén */
    @DeleteMapping("/{id}/repuestos/{idDetalle}")
    public ServicioResponse devolverRepuesto(@PathVariable Integer id, @PathVariable Integer idDetalle,
                                             @AuthenticationPrincipal Jwt jwt) {
        return servicioService.devolverRepuesto(id, idDetalle, Integer.valueOf(jwt.getSubject()));
    }
}
