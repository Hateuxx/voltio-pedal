package com.voltiopedal.apiinventario.usuario;

public final class PoliticaContrasena {
    public static final String PATRON = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,72}$";
    
    public static final String MENSAJE = "La contraseña debe tener entre 8 y 72 caracteres, e incluir al menos una letra mayúscula, una letra minúscula, un número y un carácter especial.";

    private PoliticaContrasena() {
        
    }
}
