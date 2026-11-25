package com.voltiopedal.apiinventario.ingresomercaderia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IngresoMercaderiaResponse(
    Integer idIngreso,
    String numeroGuia,
    LocalDateTime fechaHora,
    Integer idUsuario,
    String observacion,
    Boolean activo,
    Integer idProveedor,
    String rucProveedor,
    String razonSocialProveedor,
    List<Linea> detalles,
    BigDecimal total
) {
    public record Linea(
        Integer idMercaderia,
        String sku,
        String descripcion,
        Integer cantidad,
        BigDecimal costoUnitario,
        BigDecimal subtotal
    ) {}
}