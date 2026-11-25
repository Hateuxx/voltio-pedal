package com.voltiopedal.apiinventario.serviciotecnico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalleServicioResponse(
    Integer idDetalleServicio,
    Integer idServicioTecnico,

    /* Datos del repuesto usado (vienen de las tablas mercaderia y tipo_repuesto) */
    Integer idRepuesto,
    String sku,
    String descripcion,
    String nombreTipoRepuesto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal costoUnitario,
    
    /* Calculado: cantidad x precioUnitario (no existe como columna en la base) */
    BigDecimal subtotal,
    LocalDateTime fechaHora
) {}