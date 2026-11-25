package com.voltiopedal.apiinventario.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/* El PRECIO no viaja en el request: se toma del catálogo en el servidor */
public record VentaRequest(
    Integer idCliente,                 /* opcional en BOLETA, obligatorio en FACTURA */

    @NotNull(message = "El tipo de comprobante es obligatorio.")
    TipoComprobante tipoComprobante,

    @NotEmpty(message = "Debe agregar al menos un producto.")
    @Valid
    List<Linea> detalles
) {
    public record Linea(
        @NotNull(message = "Seleccione la mercadería.")
        Integer idMercaderia,

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor a 0.")
        Integer cantidad,

        @Size(max = 50, message = "El número de serie no puede superar los 50 caracteres.")
        String numeroSerie
    ) {}
}
