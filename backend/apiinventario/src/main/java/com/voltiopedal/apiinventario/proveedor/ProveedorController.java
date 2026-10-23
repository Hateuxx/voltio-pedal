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
}
