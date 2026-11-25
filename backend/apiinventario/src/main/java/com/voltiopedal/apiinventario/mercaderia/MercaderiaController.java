package com.voltiopedal.apiinventario.mercaderia;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        mercaderiaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Integer id) {
        mercaderiaService.reactivar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<Void> eliminarFisico(@PathVariable Integer id) {
        mercaderiaService.eliminarFisico(id);
        return ResponseEntity.noContent().build();
    }
}
