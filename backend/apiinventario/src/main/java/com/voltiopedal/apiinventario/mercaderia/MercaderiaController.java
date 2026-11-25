package com.voltiopedal.apiinventario.mercaderia;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.voltiopedal.apiinventario.common.MotivoRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/mercaderias")
public class MercaderiaController {
    private final MercaderiaService mercaderiaService;

    public MercaderiaController(MercaderiaService mercaderiaService) {
        this.mercaderiaService = mercaderiaService;
    }

    @PostMapping
    public ResponseEntity<MercaderiaResponse> registrar(@Valid @RequestBody MercaderiaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mercaderiaService.registrar(request));
    }

    @GetMapping
    public List<MercaderiaResponse> listarActivas() {
        return mercaderiaService.listarActivas();
    }

    @GetMapping("/inactivos")
    public List<MercaderiaResponse> listarInactivas() {
        return mercaderiaService.listarInactivas();
    }

    @GetMapping("/{id}")
    public MercaderiaResponse buscarPorId(@PathVariable Integer id) {
        return mercaderiaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public MercaderiaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody MercaderiaRequest request) {
        return mercaderiaService.actualizar(id, request);
    }

    /* DELETE /api/proveedores/{id} -> eliminación lógica (con motivo) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, @Valid @RequestBody MotivoRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        mercaderiaService.desactivar(id, request.motivo(), Integer.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Integer id, @Valid @RequestBody MotivoRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        mercaderiaService.reactivar(id, request.motivo(), Integer.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

        /* DELETE /api/proveedores/{id}/fisico -> eliminación física (con motivo) */
    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<Void> eliminarFisico(@PathVariable Integer id, @Valid @RequestBody MotivoRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        mercaderiaService.eliminarFisico(id, request.motivo(), Integer.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    /* GET /api/mercaderias/alertas-stock */
    @GetMapping("/alertas-stock")
    public List<MercaderiaResponse> alertasStock() {
        return mercaderiaService.listarStockBajo();
    }
}
