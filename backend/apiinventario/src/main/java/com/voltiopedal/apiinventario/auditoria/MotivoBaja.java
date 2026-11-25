package com.voltiopedal.apiinventario.auditoria;


public enum MotivoBaja {
    BAJA_ROTACION("Baja rotación"),
    DEFECTO("Defecto"),
    DESCONTINUADO("Descontinuado"),
    ERROR_REGISTRO("Error de registro"),
    OTRO("Otro");

    private final String etiqueta;

    MotivoBaja(String etiqueta) {
        this.etiqueta = etiqueta;
    }


    public String etiqueta() {
        return etiqueta;
    }
}