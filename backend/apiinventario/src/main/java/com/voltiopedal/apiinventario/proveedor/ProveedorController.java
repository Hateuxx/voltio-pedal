package com.voltiopedal.apiinventario.proveedor;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController 
@RequestMapping("/api/proveedores") /* En plural por convencion REST */
public class ProveedorController {
    private final ProveedorService proveedorService;

    /* Inserta contructor(recomendado) */
    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

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

    /* POST /api/proveedores */
    @PostMapping 
    public ResponseEntity<ProveedorResponse> registrar(@Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse creado = proveedorService.registrar(request);
        /* Retornamos 201 CREATED en lugar del 200 OK por defecto*/
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /* PUT /api/proveedores */
    @PutMapping("/{id}")
    public ProveedorResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProveedorRequest request) {       
        return proveedorService.actualizar(id, request);
    }

    /* DELETE /api/proveedores */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Integer id) {
        proveedorService.darDeBaja(id);
    }
}
