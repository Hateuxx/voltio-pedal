package com.voltiopedal.apiinventario.mercaderia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MercaderiaResponse(
    Integer idMercaderia,
    String tipo,
    String sku,
    String descripcion,
    Integer stock,
    Integer stockMinimo,
    BigDecimal precioCosto,
    BigDecimal precioVenta,
    LocalDateTime fechaIngreso,
    Boolean activo,
    Integer idProveedor,
    String proveedor,             /* razón social, para mostrar en la tabla */
    /* SCOOTER */
    Integer autonomiaKm,
    Integer potenciaWatts,
    String color,
    Integer mesesGarantia,
    /* REPUESTO */
    Integer idTipoRepuesto,
    String tipoRepuesto,
    String modelosCompatibles,
    /* ACCESORIO */
    String talla
) {}
