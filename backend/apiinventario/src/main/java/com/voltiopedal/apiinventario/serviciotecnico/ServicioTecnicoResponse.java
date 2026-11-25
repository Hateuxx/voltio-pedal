package com.voltiopedal.apiinventario.serviciotecnico;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ServicioTecnicoResponse(
    Integer idServicioTecnico,
    LocalDateTime fechaIngreso,
    LocalDateTime fechaEntrega,
    String numeroSerie,
    String fallaReportada,
    String diagnostico,
    EstadoServicio estado,
    LocalDate fechaCompra,
    Integer mesesGarantia,
    Boolean cubiertoPorGarantia,

    /* Datos del cliente */
    Integer idCliente,
    String numeroDocumentoCliente,
    String nombresCliente,
    String apellidosCliente,

    /* Datos del scooter */
    Integer idScooter,
    String skuScooter,
    String descripcionScooter,
    
    /* Técnico asignado (null si todavía no tiene) */
    Integer idTecnico,
    String nombresTecnico,
    String apellidosTecnico
) {}