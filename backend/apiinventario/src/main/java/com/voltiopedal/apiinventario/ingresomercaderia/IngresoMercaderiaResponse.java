package com.voltiopedal.apiinventario.ingresomercaderia;

import java.time.LocalDateTime;

public record IngresoMercaderiaResponse(
    Integer idIngreso,
    String numeroGuia,
    LocalDateTime fechaHora,
    Integer idUsuario,
    String observacion,
    Boolean activo,
    /* Datos que vienen de la tabla proveedor */
    Integer idProveedor,
    String rucProveedor,
    String razonSocialProveedor
) {}