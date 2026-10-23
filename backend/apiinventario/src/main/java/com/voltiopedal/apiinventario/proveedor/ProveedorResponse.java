package com.voltiopedal.apiinventario.proveedor;

public record ProveedorResponse(
    Integer idProveedor,
    String ruc,
    String razonSocial,
    String contacto,
    String telefono,
    String rubro,
    String frecuenciaEntrega,
    Boolean activo /* ¡Bienvenido de vuelta! */
) {}