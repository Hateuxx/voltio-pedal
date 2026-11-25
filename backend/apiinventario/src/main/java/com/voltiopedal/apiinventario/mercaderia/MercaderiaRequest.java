package com.voltiopedal.apiinventario.mercaderia;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/* Request plano: comunes + campos de cada subtipo (el servicio valida según el tipo). Sin stock: nace en 0 */
public record MercaderiaRequest(
    @NotBlank(message = "El tipo es obligatorio.")
    @Pattern(regexp = "^(SCOOTER|REPUESTO|ACCESORIO)$", message = "El tipo solo puede ser SCOOTER, REPUESTO o ACCESORIO.")
    String tipo,

    @NotBlank(message = "El SKU es obligatorio.")
    @Size(max = 50, message = "El SKU no puede superar los 50 caracteres.")
    String sku,

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres.")
    String descripcion,

    @NotNull(message = "El stock mínimo es obligatorio.")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo.")
    Integer stockMinimo,

    @NotNull(message = "El precio de costo es obligatorio.")
    @PositiveOrZero(message = "El precio de costo no puede ser negativo.")
    @Digits(integer = 8, fraction = 4, message = "El precio de costo admite hasta 8 enteros y 4 decimales.")
    BigDecimal precioCosto,

    @NotNull(message = "El precio de venta es obligatorio.")
    @PositiveOrZero(message = "El precio de venta no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El precio de venta admite hasta 8 enteros y 2 decimales.")
    BigDecimal precioVenta,

    @NotNull(message = "El proveedor es obligatorio.")
    Integer idProveedor,

    /* SCOOTER */
    @Positive(message = "La autonomía debe ser mayor a 0.")
    Integer autonomiaKm,
    @Positive(message = "La potencia debe ser mayor a 0.")
    Integer potenciaWatts,
    @Size(max = 50, message = "El color no puede superar los 50 caracteres.")
    String color,                 /* obligatorio en SCOOTER, opcional en ACCESORIO */
    @PositiveOrZero(message = "La garantía no puede ser negativa.")
    Integer mesesGarantia,

    /* REPUESTO */
    Integer idTipoRepuesto,
    @Size(max = 255, message = "Los modelos compatibles no pueden superar los 255 caracteres.")
    String modelosCompatibles,

    /* ACCESORIO */
    @Size(max = 20, message = "La talla no puede superar los 20 caracteres.")
    String talla
) {}
