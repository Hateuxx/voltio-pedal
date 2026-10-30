package com.voltiopedal.apiinventario.ingresomercaderia;

import org.springframework.web.bind.annotation.*;

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
}