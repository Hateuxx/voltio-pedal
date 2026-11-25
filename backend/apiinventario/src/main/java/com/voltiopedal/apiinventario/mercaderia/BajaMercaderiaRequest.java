package com.voltiopedal.apiinventario.mercaderia;

import com.voltiopedal.apiinventario.auditoria.MotivoBaja;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/* Cuerpo del DELETE /api/mercaderias/{id}: el motivo es obligatorio y el comentario opcional (obligatorio si es OTRO) */
public record BajaMercaderiaRequest(
    @NotNull(message = "El motivo es obligatorio")
    MotivoBaja motivo,

    @Size(max = 200, message = "El comentario no puede pasar de 200 caracteres")
    String comentario
) {}