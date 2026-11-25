package com.voltiopedal.apiinventario.reporte;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/* Formato común de los tres reportes. <T> es el tipo de fila, que cambia en cada reporte */
public record ReporteResponse<T>(
    String titulo,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    int totalRegistros,
    /* Totales del reporte (cambian según el tipo de reporte) */
    Map<String, Object> resumen,
    List<T> filas
) {}