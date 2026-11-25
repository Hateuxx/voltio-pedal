package com.voltiopedal.apiinventario.serviciotecnico;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/* Datos para registrar el ingreso de un equipo al taller */
public record ServicioTecnicoRequest(
    @NotNull(message = "El cliente es obligatorio.")
    Integer idCliente,

    @NotNull(message = "El scooter es obligatorio.")
    Integer idScooter,

    @NotBlank(message = "El número de serie es obligatorio.")
    @Size(max = 50, message = "El número de serie no puede superar los 50 caracteres.")
    String numeroSerie,

    @NotBlank(message = "La falla reportada es obligatoria.")
    @Size(max = 2000, message = "La falla reportada no puede superar los 2000 caracteres.")
    String fallaReportada,

    /* Opcional: si se informa, se calcula si el equipo está en garantía */
    @PastOrPresent(message = "La fecha de compra no puede ser futura.")
    LocalDate fechaCompra,

    /* Opcional: si no se envía, se usan los meses de garantía del scooter */
    @PositiveOrZero(message = "Los meses de garantía no pueden ser negativos.")
    Integer mesesGarantia,

    /* Opcional: técnico asignado desde el inicio */
    Integer idTecnico
) {}