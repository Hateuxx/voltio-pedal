package com.voltiopedal.apiinventario.tiporepuesto;

import jakarta.validation.constraints.*;

public record TipoRepuestoRequest(
    @NotBlank (message = "El nombre es obligatorio.")
    @Size (max=100, message = "El nombre no debe superar los 100 caracteres")
    String nombre
) {}