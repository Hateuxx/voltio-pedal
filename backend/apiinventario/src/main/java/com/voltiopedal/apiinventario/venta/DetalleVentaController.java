package com.voltiopedal.apiinventario.venta;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class DetalleVentaController {
    private final DetalleVentaService detalleVentaService;

    /* Insertar dependencia por constructor */
    public DetalleVentaController(DetalleVentaService detalleVentaService) {
        this.detalleVentaService = detalleVentaService;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/ventas/{idVenta}/detalles */
    @GetMapping("/{idVenta}/detalles")
    public List<DetalleVentaResponse> listarPorVenta(@PathVariable Integer idVenta) {
        return detalleVentaService.listarPorVenta(idVenta);
    }
}