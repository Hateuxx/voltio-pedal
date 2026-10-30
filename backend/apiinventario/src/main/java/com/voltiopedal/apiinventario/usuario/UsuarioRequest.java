package com.voltiopedal.apiinventario.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotNull(message = "El tipo de documento no puede ser nulo")
    TipoDocumento tipoDocumento,

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no debe superar los 20 caracteres")
    String numeroDocumento,

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no deben superar los 100 caracteres")
    String nombres,

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no deben superar los 100 caracteres")
    String apellidos,

    @NotNull(message = "El rol es obligatorio")
    Rol rol,

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = PoliticaContrasena.PATRON, message = PoliticaContrasena.MENSAJE)
    String contrasena,

    @NotBlank(message = "La pregunta de seguridad es obligatoria")
    @Size(max = 150, message = "La pregunta de seguridad no debe superar los 150 caracteres")
    String preguntaSeguridad,

    @NotBlank(message = "La respuesta de seguridad es obligatoria")
    String respuestaSeguridad
    
) {}