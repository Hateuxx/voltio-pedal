package com.voltiopedal.apiinventario.inventario;

import java.time.LocalDateTime;

public record MovimientoResponse(
    Integer idMovimiento,
    LocalDateTime fechaHora,
    TipoMovimiento tipo,
    boolean entrada,          /* true = suma stock, false = resta */
    Integer cantidad,
    Integer stockAnterior,
    Integer stockResultante,
    String usuario,           /* nombres y apellidos */
    String origen,            /* "Ingreso #4", "Venta #10"... */
    String observacion
) {}