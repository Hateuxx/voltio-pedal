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

    /* Insertar dependencia por constructor */
    public MercaderiaController(MercaderiaService mercaderiaService) {
        this.mercaderiaService = mercaderiaService;
    }

    /* ==============================
     *          C - CREAR
     * ============================== */

    /* POST /api/mercaderias */
    @PostMapping
    public ResponseEntity<MercaderiaResponse> registrar(@Valid @RequestBody MercaderiaRequest request) {
        MercaderiaResponse creada = mercaderiaService.registrar(request);
        /* 201 CREATED en lugar del 200 OK por defecto */
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/mercaderias */
    @GetMapping
    public List<MercaderiaResponse> listarActivas() {
        return mercaderiaService.listarActivas();
    }

    /* GET /api/mercaderias/{id} */
    @GetMapping("/{id}")
    public MercaderiaResponse buscarPorId(@PathVariable Integer id) {
        return mercaderiaService.buscarPorId(id);
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    /* PUT /api/mercaderias/{id} */
    @PutMapping("/{id}")
    public MercaderiaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody MercaderiaRequest request) {
        return mercaderiaService.actualizar(id, request);
    }

    /* ==============================
     *       D - ELIMINAR LÓGICO
     * ============================== */

    /* DELETE /api/mercaderias/{id} -> desactiva, no borra. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, @Valid @RequestBody BajaMercaderiaRequest request) {
        mercaderiaService.desactivar(id, request);
        return ResponseEntity.noContent().build();
    }

    /* PATCH /api/mercaderias/{id}/reactivar -> vuelve a activar. El comentario es opcional */
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Integer id,
                                          @Valid @RequestBody(required = false) ReactivarMercaderiaRequest request) {
        mercaderiaService.reactivar(id, request == null ? null : request.comentario());
        return ResponseEntity.noContent().build();
    }
}