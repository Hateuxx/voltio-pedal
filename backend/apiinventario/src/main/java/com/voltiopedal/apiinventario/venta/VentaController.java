package com.voltiopedal.apiinventario.venta;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    private final VentaService ventaService;

    /* Insertar dependencia por constructor */
    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/ventas */
    @GetMapping
    public List<VentaResponse> listarActivas() {
        return ventaService.listarActivas();
    }

    /* GET /api/ventas/{id} */
    @GetMapping("/{id}")
    public VentaResponse buscarPorId(@PathVariable Integer id) {
        return ventaService.buscarPorId(id);
    }
}