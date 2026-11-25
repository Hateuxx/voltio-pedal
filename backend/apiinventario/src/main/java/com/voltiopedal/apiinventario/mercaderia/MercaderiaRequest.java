package com.voltiopedal.apiinventario.mercaderia;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/* Un solo Request para los tres tipos. Los campos que no aplican al tipo se envían vacíos */
public record MercaderiaRequest(
    @NotNull(message = "El tipo es obligatorio (SCOOTER, REPUESTO o ACCESORIO).")
    TipoMercaderia tipo,

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
    @DecimalMin(value = "0.00", message = "El precio de costo no puede ser negativo.")
    @Digits(integer = 8, fraction = 4, message = "El precio de costo admite hasta 4 decimales.")
    BigDecimal precioCosto,

    @NotNull(message = "El precio de venta es obligatorio.")
    @DecimalMin(value = "0.00", message = "El precio de venta no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El precio de venta admite hasta 2 decimales.")
    BigDecimal precioVenta,

    @NotNull(message = "El proveedor es obligatorio.")
    Integer idProveedor,

    /* Solo scooter */
    @Positive(message = "La autonomía debe ser mayor a 0.")
    Integer autonomiaKm,

    @Positive(message = "La potencia debe ser mayor a 0.")
    Integer potenciaWatts,

    @PositiveOrZero(message = "Los meses de garantía no pueden ser negativos.")
    Integer mesesGarantia,

    /* Scooter y accesorio */
    @Size(max = 50, message = "El color no puede superar los 50 caracteres.")
    String color,

    /* Solo repuesto */
    Integer idTipoRepuesto,

    @Size(max = 255, message = "Los modelos compatibles no pueden superar los 255 caracteres.")
    String modelosCompatibles,

    /* Solo accesorio */
    @Size(max = 20, message = "La talla no puede superar los 20 caracteres.")
    String talla
) {}