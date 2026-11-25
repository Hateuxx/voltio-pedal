package com.voltiopedal.apiinventario.reporte;

import java.math.BigDecimal;

/* Un dato destacado de un reporte: por ejemplo, "BOLETA B001-0002" con su monto, o "SCO-001 - Scooter..." con sus unidades */
public record Destacado(String nombre, BigDecimal valor) {}