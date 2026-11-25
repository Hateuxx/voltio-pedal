package com.voltiopedal.apiinventario.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResponse(
    Integer idVenta,
    TipoComprobante tipoComprobante,
    String numeroComprobante,
    LocalDateTime fechaHora,
    BigDecimal total,
    Integer idUsuario,
    Boolean activo,
    /* Datos que vienen de la tabla cliente (null si es venta al paso) */
    Integer idCliente,
    String numeroDocumentoCliente,
    String nombresCliente,
    String apellidosCliente
) {}