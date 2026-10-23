package com.voltiopedal.apiinventario.proveedor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
    @NotBlank(message = "El RUC es obligatorio.")
    @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener exactamente 11 números.")
    String ruc,

    @NotBlank(message = "La razón social es obligatoria.")
    @Size(max = 255, message = "La razón social no puede superar los 255 caracteres.")
    String razonSocial,

    @Size(max = 100, message = "El nombre del contacto no puede superar los 100 caracteres.")
    String contacto,

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.")
    String telefono,

    @Size(max = 100, message = "El rubro no puede superar los 100 caracteres.")
    String rubro,

    @NotBlank(message = "La frecuencia de entrega es obligatoria.")
    @Pattern(regexp = "^(SEMANAL|QUINCENAL)$", message = "La frecuencia de entrega solo puede ser 'SEMANAL' o 'QUINCENAL'.")
    String frecuenciaEntrega
) {}