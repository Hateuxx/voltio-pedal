package com.voltiopedal.apiinventario.serviciotecnico;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios-tecnicos")
public class ServicioTecnicoController {
    private final ServicioTecnicoService servicioService;

    /* Insertar dependencia por constructor */
    public ServicioTecnicoController(ServicioTecnicoService servicioService) {
        this.servicioService = servicioService;
    }

    /* ==============================
     *          C - CREAR
     * ============================== */

    /* POST /api/servicios-tecnicos */
    @PostMapping
    public ResponseEntity<ServicioTecnicoResponse> registrar(@Valid @RequestBody ServicioTecnicoRequest request) {
        ServicioTecnicoResponse creado = servicioService.registrar(request);
        /* 201 CREATED en lugar del 200 OK por defecto */
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/servicios-tecnicos */
    @GetMapping
    public List<ServicioTecnicoResponse> listar() {
        return servicioService.listar();
    }

    /* GET /api/servicios-tecnicos/{id} */
    @GetMapping("/{id}")
    public ServicioTecnicoResponse buscarPorId(@PathVariable Integer id) {
        return servicioService.buscarPorId(id);
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    /* PUT /api/servicios-tecnicos/{id} */
    @PutMapping("/{id}")
    public ServicioTecnicoResponse actualizar(@PathVariable Integer id,
                                              @Valid @RequestBody ServicioTecnicoActualizacionRequest request) {
        return servicioService.actualizar(id, request);
    }
}