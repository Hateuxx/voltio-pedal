package com.voltiopedal.apiinventario.movimiento;

import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
    Integer idMovimiento,
    LocalDateTime fechaHora,
    TipoMovimiento tipo,

    /* true = suma al stock (entrada), false = resta (salida) */
    Boolean esEntrada,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockResultante,

    /* Datos del producto (vienen de la tabla mercaderia) */
    Integer idMercaderia,
    String sku,
    String descripcion,
    Integer idUsuario,
    
    /* Origen: solo uno viene lleno (los demás null) */
    Integer idVenta,
    Integer idServicioTecnico,
    Integer idIngreso,
    Integer idConteo,
    String observacion
) {}