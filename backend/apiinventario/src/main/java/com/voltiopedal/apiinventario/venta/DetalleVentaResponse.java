package com.voltiopedal.apiinventario.venta;

import java.math.BigDecimal;

public record DetalleVentaResponse(
    Integer idDetalleVenta,
    Integer idVenta,
    
    /* Datos del producto vendido (vienen de la tabla mercaderia) */
    Integer idMercaderia,
    String sku,
    String descripcion,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal costoUnitario,

    /* Calculado: cantidad x precioUnitario (no existe como columna en la base) */
    BigDecimal subtotal,

    /* Solo si se vendió una unidad con número de serie (si no, null) */
    String numeroSerie
) {}
