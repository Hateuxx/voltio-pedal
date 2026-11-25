package com.voltiopedal.apiinventario.mercaderia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MercaderiaResponse(
    Integer idMercaderia,
    TipoMercaderia tipo,
    String sku,
    String descripcion,
    Integer stock,
    Integer stockMinimo,
    BigDecimal precioCosto,
    BigDecimal precioVenta,
    LocalDateTime fechaIngreso,
    Boolean activo,

    /* Datos del proveedor */
    Integer idProveedor,
    String razonSocialProveedor,

    /* Solo scooter (null en los demás) */
    Integer autonomiaKm,
    Integer potenciaWatts,
    Integer mesesGarantia,

    /* Scooter y accesorio */
    String color,

    /* Solo repuesto */
    Integer idTipoRepuesto,
    String nombreTipoRepuesto,
    String modelosCompatibles,
    
    /* Solo accesorio */
    String talla,

    /* Solo si está desactivado y existe su registro en la auditoría (null en los demás casos) */
    InfoBaja baja
) {
    /* Datos de la baja: motivo, comentario, cuándo y quién */
    public record InfoBaja(String motivo, String comentario, LocalDateTime fecha, String usuario) {}
}