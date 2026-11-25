package com.voltiopedal.apiinventario.ingresomercaderia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record IngresoRequest(
    @NotNull(message = "El proveedor es obligatorio.")
    Integer idProveedor,

    @NotBlank(message = "El número de guía es obligatorio.")
    @Size(max = 20, message = "El número de guía no puede superar los 20 caracteres.")
    String numeroGuia,

    @Size(max = 255, message = "La observación no puede superar los 255 caracteres.")
    String observacion,

    @NotEmpty(message = "Debe agregar al menos un producto.")
    @Valid                       /* valida también CADA línea de la lista */
    List<Linea> detalles
) {
    public record Linea(
        @NotNull(message = "Seleccione la mercadería.")
        Integer idMercaderia,

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor a 0.")
        Integer cantidad,

        @NotNull(message = "El costo unitario es obligatorio.")
        @PositiveOrZero(message = "El costo no puede ser negativo.")
        @Digits(integer = 8, fraction = 4, message = "El costo admite hasta 8 enteros y 4 decimales.")
        BigDecimal costoUnitario
    ) {}
}