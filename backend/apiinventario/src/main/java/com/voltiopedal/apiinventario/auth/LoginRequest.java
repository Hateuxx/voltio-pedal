package com.voltiopedal.apiinventario.auth;

import com.voltiopedal.apiinventario.usuario.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotNull(message = "El tipo de documento es obligatorio")
    TipoDocumento tipoDocumento,

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    String numeroDocumento,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 72, message = "La contraseña no puede exceder los 72 caracteres") //Limite de BCrypt
    String contrasena
) {}
