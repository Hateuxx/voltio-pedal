package com.voltiopedal.apiinventario.mercaderia;

import jakarta.validation.constraints.Size;

/* Cuerpo (opcional) del PATCH /api/mercaderias/{id}/reactivar */
public record ReactivarMercaderiaRequest(
    @Size(max = 200, message = "El comentario no puede pasar de 200 caracteres")
    String comentario
) {}