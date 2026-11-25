package com.voltiopedal.apiinventario.ingresomercaderia;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoMercaderiaController {
    private final IngresoMercaderiaService ingresoMercaderiaService;

    /* Insertar dependencia por constructor */
    public IngresoMercaderiaController(IngresoMercaderiaService ingresoMercaderiaService) {
        this.ingresoMercaderiaService = ingresoMercaderiaService;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/ingresos */
    @GetMapping
    public List<IngresoMercaderiaResponse> listarActivos() {
        return ingresoMercaderiaService.listarActivos();
    }

    /* GET /api/ingresos/{id} */
    @GetMapping("/{id}")
    public IngresoMercaderiaResponse buscarPorId(@PathVariable Integer id) {
        return ingresoMercaderiaService.buscarPorId(id);
    }

        /* POST /api/ingresos: el usuario sale del token, NO del body */
    @PostMapping
    public ResponseEntity<IngresoMercaderiaResponse> registrar(@Valid @RequestBody IngresoRequest request,
                                                               @AuthenticationPrincipal Jwt jwt) {
        Integer idUsuario = Integer.valueOf(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(ingresoMercaderiaService.registrar(request, idUsuario));
    }
}