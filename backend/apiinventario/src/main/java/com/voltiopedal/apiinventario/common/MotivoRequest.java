package com.voltiopedal.apiinventario.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* Cuerpo para acciones que exigen justificación (baja, reactivación, eliminación) */
public record MotivoRequest(
    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres.")
    String motivo
) {}