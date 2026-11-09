package com.voltiopedal.apiinventario.auth;

import com.voltiopedal.apiinventario.usuario.TipoDocumento;
import com.voltiopedal.apiinventario.usuario.PoliticaContrasena;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestablecerRequest(
    @NotNull(message = "El tipo de documento es obligatorio")
    TipoDocumento tipoDocumento,

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    String numeroDocumento,

    @NotBlank(message = "La respuesta de seguridad es obligatoria")
    @Size(max = 72, message = "La respuesta de seguridad no debe superar los 72 caracteres")
    String respuestaSeguridad,

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Pattern(regexp = PoliticaContrasena.PATRON, message = PoliticaContrasena.MENSAJE)
    String nuevaContrasena
) {}
