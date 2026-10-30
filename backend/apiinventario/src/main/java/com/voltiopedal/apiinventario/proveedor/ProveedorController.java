package com.voltiopedal.apiinventario.proveedor;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores") /* En plural por convencion REST */
public class ProveedorController {
    private final ProveedorService proveedorService;

    /* Inserta contructor(recomendado) */
    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    /* ==============================
     *          C - CREAR
     * ============================== */

    /* POST /api/proveedores */
    @PostMapping
    public ResponseEntity<ProveedorResponse> registrar(@Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse creado = proveedorService.registrar(request);
        /* Retornamos 201 CREATED en lugar del 200 OK por defecto */
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/proveedores */
    @GetMapping
    public List<ProveedorResponse> listarActivos() {
        return proveedorService.listarActivos();
    }

    /* GET /api/proveedores/ruc/{ruc} */
    @GetMapping("/ruc/{ruc}")
    public ProveedorResponse buscarPorRuc(@PathVariable String ruc) {
        return proveedorService.buscarPorRuc(ruc);
    }

    /* GET /api/proveedores/{id} */
    @GetMapping("/{id}")
    public ProveedorResponse buscarPorId(@PathVariable Integer id) {
        return proveedorService.buscarPorId(id);
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    /* PUT /api/proveedores/{id} */
    @PutMapping("/{id}")
    public ProveedorResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProveedorRequest request) {
        return proveedorService.actualizar(id, request);
    }

    /* ==============================
     *       D - ELIMINAR LÓGICO
     * ============================== */

    /* DELETE /api/proveedores/{id} -> eliminación lógica */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        proveedorService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    /* ==============================
     *       D - ELIMINAR FÍSICO
     * ============================== */

    /* DELETE /api/proveedores/{id}/fisico -> eliminación física */
    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<Void> eliminarFisico(@PathVariable Integer id) {
        proveedorService.eliminarFisico(id);
        return ResponseEntity.noContent().build();
    }
}