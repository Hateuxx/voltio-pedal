package com.voltiopedal.apiinventario.serviciotecnico;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios-tecnicos")
public class DetalleServicioController {
    private final DetalleServicioService detalleService;

    /* Insertar dependencia por constructor */
    public DetalleServicioController(DetalleServicioService detalleService) {
        this.detalleService = detalleService;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/servicios-tecnicos/{idServicio}/detalles */
    @GetMapping("/{idServicio}/detalles")
    public List<DetalleServicioResponse> listarPorServicio(@PathVariable Integer idServicio) {
        return detalleService.listarPorServicio(idServicio);
    }
}