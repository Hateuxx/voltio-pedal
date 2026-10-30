package com.voltiopedal.apiinventario.cliente;

import jakarta.validation.constraints.*;


public record ClienteRequest(
    
    @NotNull (message = "El tipo de documento es obligatorio")
    TipoDocumentoCliente tipoDocumento,

    @NotBlank (message = "El número de documento es obligatorio")
    @Size (max = 20, message = "El número de documento no debe superar los 20 caracteres")
    String numeroDocumento,

    @NotBlank (message = "Los nombres son obligatorios")
    @Size (max = 100, message = "Los nombres o la razón social no pueden superar los 100 caracteres")
    String nombres,

    @Size (max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    String apellidos,
    
    @Size (max = 20, message = "El teléfono no puede superar los 20 caracteres")
    String telefono
) {} 