package com.voltiopedal.apiinventario.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ServicioResponse(
    Integer idServicio,
    LocalDateTime fechaIngreso,
    LocalDateTime fechaEntrega,
    String numeroSerie,
    String fallaReportada,
    String diagnostico,
    EstadoServicio estado,
    LocalDate fechaCompra,
    Integer mesesGarantia,
    LocalDate garantiaHasta,
    Boolean cubiertoPorGarantia,
    Integer idCliente,
    String cliente,
    Integer idScooter,
    String scooter,
    Integer idTecnico,
    String tecnico,
    List<Repuesto> repuestos,
    BigDecimal totalRepuestos,
    BigDecimal totalACobrar       /* 0 si está cubierto por garantía */
) {
    public record Repuesto(
        Integer idDetalle,
        Integer idRepuesto,
        String sku,
        String descripcion,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        LocalDateTime fechaHora
    ) {}

    public record Tecnico(Integer idUsuario, String nombre) {}
}
