package com.voltiopedal.apiinventario.reporte;

import com.voltiopedal.apiinventario.inventario.TipoMovimiento;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/* Reportes gerenciales (solo admin, ver SecurityConfig). Fechas en formato ISO: 2026-11-25 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /* Inventario valorizado al costo y al precio de venta */
    @GetMapping("/inventario")
    public ReporteResponses.Inventario inventario() {
        return reporteService.inventario();
    }

    /* Ventas, costo y utilidad por rango (por defecto, el mes actual) */
    @GetMapping("/ventas")
    public ReporteResponses.Ventas ventas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.ventas(desde, hasta);
    }

    /* Kardex general del almacén (por defecto, últimos 30 días); tipo opcional */
    @GetMapping("/movimientos")
    public List<ReporteResponses.Movimiento> movimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo) {
        return reporteService.movimientos(desde, hasta, tipo);
    }

    /* Servicios técnicos: por estado, por técnico y repuestos consumidos */
    @GetMapping("/servicios")
    public ReporteResponses.Servicios servicios(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.servicios(desde, hasta);
    }
}
