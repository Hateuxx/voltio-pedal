package com.voltiopedal.apiinventario.auth;

import java.time.Instant;

public class CuentaBloqueadaException extends RuntimeException {
    private final Instant bloqueadoHasta;

    public CuentaBloqueadaException(String message, Instant bloqueadoHasta) {
        super(message);
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Instant getBloqueadoHasta () {
        return bloqueadoHasta;
    }
}
