package com.voltiopedal.apiinventario.conteo;

import java.time.LocalDateTime;
import java.util.List;

public record ConteoResponse(
    Integer idConteo,
    LocalDateTime fechaInicio,
    LocalDateTime fechaCierre,
    EstadoConteo estado,
    Integer idUsuario,
    String observacion,
    List<Linea> lineas,
    int productosContados,
    int conDiferencia,
    Integer exactitud           /* % de productos sin diferencia; null si no hay líneas */
) {
    public record Linea(
        Integer idMercaderia,
        String sku,
        String descripcion,
        Integer stockSistema,
        Integer stockFisico,
        int diferencia,
        Boolean ajustado
    ) {}
}