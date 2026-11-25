package com.voltiopedal.apiinventario.conteo;

import jakarta.validation.constraints.*;

public final class ConteoRequests {

    private ConteoRequests() { }

    public record Abrir(
        @Size(max = 255, message = "La observación no puede superar los 255 caracteres.")
        String observacion
    ) {}

    public record Linea(
        @NotNull(message = "Seleccione la mercadería.")
        Integer idMercaderia,

        @NotNull(message = "El stock físico es obligatorio.")
        @PositiveOrZero(message = "El stock físico no puede ser negativo.")
        Integer stockFisico
    ) {}

    public record Cerrar(
        @NotBlank(message = "El motivo es obligatorio para aplicar los ajustes.")
        @Size(max = 200, message = "El motivo no puede superar los 200 caracteres.")
        String motivo
    ) {}
}