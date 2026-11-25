package com.voltiopedal.apiinventario.common;

import java.util.Map;

/* Errores de validación que dependen de reglas del servicio (no de anotaciones) -> 400 con mapa de errores */
public class ValidacionException extends RuntimeException {
    private final Map<String, String> errores;

    public ValidacionException(Map<String, String> errores) {
        super("Datos de entrada inválidos");
        this.errores = Map.copyOf(errores);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
