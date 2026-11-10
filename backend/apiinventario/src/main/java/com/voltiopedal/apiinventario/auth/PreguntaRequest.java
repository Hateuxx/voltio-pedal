package com.voltiopedal.apiinventario.auth;

import com.voltiopedal.apiinventario.usuario.TipoDocumento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PreguntaRequest(
    @NotNull(message = "El tipo de documento es requerido")
    TipoDocumento tipoDocumento,

    @NotBlank(message = "El número de documento es requerido")
    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    String numeroDocumento
) {}
