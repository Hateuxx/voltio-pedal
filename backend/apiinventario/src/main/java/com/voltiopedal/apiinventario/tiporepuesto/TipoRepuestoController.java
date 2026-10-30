package com.voltiopedal.apiinventario.tiporepuesto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/tipos-repuesto")
public class TipoRepuestoController {
    private final TipoRepuestoService tipoRepuestoService;

    /*constructor */
    public TipoRepuestoController(TipoRepuestoService tipoRepuestoService){
        this.tipoRepuestoService=tipoRepuestoService;
    }

    /* ==============================
     *          C - CREAR
     * ============================== */

    /* POST /api/tipos-repuesto */
    @PostMapping 
    public ResponseEntity<TipoRepuestoResponse> registrar(@Valid @RequestBody TipoRepuestoRequest request){
        TipoRepuestoResponse creado = tipoRepuestoService.registrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/tipos-repuesto */
    @GetMapping 
    public List<TipoRepuestoResponse> listar(){
        return tipoRepuestoService.listar();
    }

    /* GET /api/tipos-repuesto/{id} */
    @GetMapping("/{id}")
    public TipoRepuestoResponse buscarPorId(@PathVariable Integer id) {
        return tipoRepuestoService.buscarPorId(id);
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    /* PUT /api/tipos-repuesto/{id} */
    @PutMapping("/{id}")
    public TipoRepuestoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoRepuestoRequest request) {
        return tipoRepuestoService.actualizar(id, request);
    }

    /* ==============================
     *       D - ELIMINAR FÍSICO
     * ============================== */

    /* DELETE /api/tipos-repuesto/{id} -> eliminación física */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFisico(@PathVariable Integer id) {
        tipoRepuestoService.eliminarFisico(id);
        return ResponseEntity.noContent().build();
    }
}