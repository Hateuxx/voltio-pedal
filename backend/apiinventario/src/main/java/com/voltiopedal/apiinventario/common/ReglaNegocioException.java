package com.voltiopedal.apiinventario.common;

public class ReglaNegocioException extends RuntimeException{
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }   
}
