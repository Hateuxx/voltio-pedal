package com.voltiopedal.apiinventario.usuario;

public record UsuarioResponse(
    Integer idUsuario,
    TipoDocumento tipoDocumento,
    String numeroDocumento,
    String nombres,
    String apellidos,
    Rol rol,
    Boolean activo
) {}
