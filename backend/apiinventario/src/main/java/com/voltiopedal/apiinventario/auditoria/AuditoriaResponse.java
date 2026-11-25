package com.voltiopedal.apiinventario.auditoria;

import java.time.LocalDateTime;

public record AuditoriaResponse(
    Integer idRegistroAuditoria,
    LocalDateTime fechaHora,
    String usuario,
    AccionAuditoria accion,
    String entidadAfectada,
    String idRegistro,
    String motivo
) {}