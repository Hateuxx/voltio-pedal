package com.voltiopedal.apiinventario.reporte;

import com.voltiopedal.apiinventario.movimiento.TipoMovimiento;
import com.voltiopedal.apiinventario.serviciotecnico.EstadoServicio;
import com.voltiopedal.apiinventario.venta.TipoComprobante;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* Las filas de los reportes, agrupadas en un solo archivo */
public final class FilasReporte {

    private FilasReporte() {
    }

    /* Reporte de ventas: una fila por venta */
    @Getter @AllArgsConstructor
    public static class VentaFila {
        private final LocalDateTime fecha;
        private final TipoComprobante tipoComprobante;
        private final String numeroComprobante;
        private final String cliente;
        private final BigDecimal total;
    }

    /* Reportes de entradas y de salidas: una fila por movimiento de inventario */
    @Getter @AllArgsConstructor
    public static class MovimientoFila {
        private final LocalDateTime fecha;
        private final TipoMovimiento tipo;
        private final Boolean esEntrada;
        private final String sku;
        private final String descripcion;
        private final Integer cantidad;
        private final Integer stockAnterior;
        private final Integer stockResultante;
        private final String observacion;
    }

    /* Reporte de servicio técnico: una fila por servicio */
    @Getter @AllArgsConstructor
    public static class ServicioFila {
        private final LocalDateTime fechaIngreso;
        private final LocalDateTime fechaEntrega;
        private final String cliente;
        private final String scooter;
        private final String numeroSerie;
        private final String fallaReportada;
        private final EstadoServicio estado;
        private final String tecnico;
        private final Boolean cubiertoPorGarantia;
    }

    /* Reporte de ventas con productos: una fila por producto vendido */
    @Getter @AllArgsConstructor
    public static class VentaDetalleFila {
        private final LocalDateTime fecha;
        private final TipoComprobante tipoComprobante;
        private final String numeroComprobante;
        private final String cliente;
        private final String sku;
        private final String descripcion;
        private final Integer cantidad;
        private final BigDecimal precioUnitario;
        private final BigDecimal subtotal;
        private final BigDecimal totalVenta;
    }

    /* Reporte de registros eliminados: una fila por cada baja (inactivación) registrada en la auditoría */
    @Getter @AllArgsConstructor
    public static class EliminadoFila {
        private final LocalDateTime fechaBaja;
        private final String sku;
        private final String descripcion;
        private final String categoria;
        private final String motivo;
        private final String comentario;
        private final String usuario;
        /* "Desactivado" o "Reactivado" */
        private final String estadoActual;
        /* Stock actual del producto (no cambia mientras está desactivado); null si el producto ya no existe */
        private final Integer stock;
        /* true si después de esta baja hubo una reactivación del mismo producto */
        private final Boolean reactivado;
        private final LocalDateTime fechaReactivacion;
    }

    /* Estado de un producto según su stock final del periodo */
    public enum EstadoStock {
        NORMAL,
        BAJO_MINIMO,
        AGOTADO
    }

    /* Reporte de stock: una fila por producto */
    @Getter @AllArgsConstructor
    public static class StockFila {
        private final String sku;
        private final String descripcion;
        private final String tipo;
        private final Integer stockInicial;
        private final Integer entradas;
        private final Integer salidas;
        private final Integer stockFinal;
        private final Integer stockMinimo;
        private final EstadoStock estado;
        /* Regla A: stock final menos (inicial + entradas - salidas). 0 = cuadra */
        private final Integer diferenciaMovimientos;
        /* Regla B: stock actual del producto menos stock final del reporte. null si el rango no llega hasta hoy */
        private final Integer diferenciaStockActual;
        private final Boolean descuadre;
    }
}