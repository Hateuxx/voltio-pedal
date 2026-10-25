package com.voltiopedal.apiinventario.cliente;

public record ClienteResponse(
    Integer idCliente,
    TipoDocumentoCliente tipoDocumento,
    String numeroDocumento,
    String nombres,
    String apellidos,
    String telefono
) {} 