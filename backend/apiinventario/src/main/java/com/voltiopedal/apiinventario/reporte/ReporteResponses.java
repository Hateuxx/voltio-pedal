package com.voltiopedal.apiinventario.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/* Respuestas de los reportes gerenciales (solo lectura) */
public final class ReporteResponses {

    private ReporteResponses() { }

    /* ---------- Inventario valorizado ---------- */
    public record Inventario(
        LocalDateTime generado,
        int productos,
        int unidades,
        int bajoMinimo,
        BigDecimal valorCosto,
        BigDecimal valorVenta,
        List<LineaInventario> lineas
    ) {}

    public record LineaInventario(
        String sku, String descripcion, String tipo, String proveedor,
        Integer stock, Integer stockMinimo, BigDecimal costoUnitario, BigDecimal precioVenta,
        BigDecimal valorCosto, BigDecimal valorVenta, boolean bajoMinimo
    ) {}

    /* ---------- Ventas ---------- */
    public record Ventas(
        LocalDate desde, LocalDate hasta,
        int cantidadVentas, int ventasAnuladas,
        BigDecimal totalVendido, BigDecimal costoTotal, BigDecimal utilidad, BigDecimal margen,
        List<ProductoVendido> productos,
        List<VentaDia> porDia
    ) {}

    public record ProductoVendido(String sku, String descripcion, int cantidad, BigDecimal importe, BigDecimal utilidad) {}

    public record VentaDia(LocalDate fecha, int ventas, BigDecimal total) {}

    /* ---------- Movimientos (kardex general) ---------- */
    public record Movimiento(
        LocalDateTime fechaHora, String tipo, boolean entrada, String sku, String descripcion,
        Integer cantidad, Integer stockAnterior, Integer stockResultante, String usuario, String origen, String observacion
    ) {}

    /* ---------- Servicio técnico ---------- */
    public record Servicios(
        LocalDate desde, LocalDate hasta,
        int total, int entregados, int cubiertosPorGarantia,
        Map<String, Long> porEstado,
        List<ServiciosTecnico> porTecnico,
        List<RepuestoConsumido> repuestos,
        BigDecimal importeRepuestos, BigDecimal importeCobrado
    ) {}

    public record ServiciosTecnico(String tecnico, long asignados, long entregados) {}

    public record RepuestoConsumido(String sku, String descripcion, int cantidad, BigDecimal importe) {}
}
