package com.voltiopedal.apiinventario.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
    Integer idVenta,
    TipoComprobante tipoComprobante,
    String numeroComprobante,
    LocalDateTime fechaHora,
    BigDecimal total,
    Boolean activo,
    Integer idCliente,
    String cliente,            /* "Nombre Apellido (DNI 12345678)" o null si fue al paso */
    Integer idUsuario,
    String vendedor,
    List<Linea> detalles
) {
    public record Linea(
        Integer idMercaderia,
        String sku,
        String descripcion,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String numeroSerie
    ) {}
}
