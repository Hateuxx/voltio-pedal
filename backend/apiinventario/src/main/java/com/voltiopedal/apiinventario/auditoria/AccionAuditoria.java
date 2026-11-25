package com.voltiopedal.apiinventario.auditoria;

/* Las acciones que acepta la columna "accion" de registro_auditoria (CHECK de la base de datos) */
public enum AccionAuditoria {
    INACTIVACION,
    REACTIVACION,
    ELIMINACION_FISICA
}
