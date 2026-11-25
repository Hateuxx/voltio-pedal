package com.voltiopedal.apiinventario.serviciotecnico;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/* Datos que cambian durante la reparación */
public record ServicioTecnicoActualizacionRequest(
    @NotNull(message = "El estado es obligatorio.")
    EstadoServicio estado,

    /* Opcional: si no se envía, se conserva el diagnóstico actual */
    @Size(max = 2000, message = "El diagnóstico no puede superar los 2000 caracteres.")
    String diagnostico,

    /* Opcional: si no se envía, se conserva el técnico actual */
    Integer idTecnico
) {}