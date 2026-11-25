package com.voltiopedal.apiinventario.servicio;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public final class ServicioRequests {

    private ServicioRequests() { }

    /* Recepción del scooter en taller */
    public record Registrar(
        @NotNull(message = "El cliente es obligatorio.")
        Integer idCliente,

        @NotNull(message = "Seleccione el modelo de scooter.")
        Integer idScooter,

        @NotBlank(message = "El número de serie es obligatorio.")
        @Size(max = 50, message = "El número de serie no puede superar los 50 caracteres.")
        String numeroSerie,

        @NotBlank(message = "Describa la falla reportada por el cliente.")
        @Size(max = 2000, message = "La falla no puede superar los 2000 caracteres.")
        String fallaReportada,

        /* Solo si el scooter NO se vendió en la tienda (si se vendió, se toman de la venta) */
        @PastOrPresent(message = "La fecha de compra no puede ser futura.")
        LocalDate fechaCompra,

        @PositiveOrZero(message = "Los meses de garantía no pueden ser negativos.")
        Integer mesesGarantia,

        Integer idTecnico
    ) {}

    /* Avance del técnico: estado, diagnóstico y técnico asignado */
    public record Actualizar(
        @NotNull(message = "El estado es obligatorio.")
        EstadoServicio estado,

        @Size(max = 2000, message = "El diagnóstico no puede superar los 2000 caracteres.")
        String diagnostico,

        Integer idTecnico
    ) {}

    /* RF17: consumo de un repuesto */
    public record Consumo(
        @NotNull(message = "Seleccione el repuesto.")
        Integer idRepuesto,

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor a 0.")
        Integer cantidad
    ) {}
}
