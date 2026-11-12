package com.voltiopedal.apiinventario.auth;

import com.voltiopedal.apiinventario.usuario.PoliticaContrasena;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest (
    @NotBlank(message = "La contraseña actual es obligatoria")
    @Size(max = 72, message = "La contraseña actual no puede exceder los 72 caracteres")
    String contrasenaActual,

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Pattern(regexp = PoliticaContrasena.PATRON, message = PoliticaContrasena.MENSAJE)
    String nuevaContrasena
) {}
