package com.voltiopedal.apiinventario.inventario;

/* Espejo del CHECK ck_mov_tipo. Cada tipo sabe si SUMA o RESTA stock */
public enum TipoMovimiento {
    ENTRADA_PROVEEDOR(true),
    DEVOLUCION_PROVEEDOR(false),
    SALIDA_VENTA(false),
    DEVOLUCION_VENTA(true),
    SALIDA_REPARACION(false),
    DEVOLUCION_REPARACION(true),
    AJUSTE_ENTRADA(true),
    AJUSTE_SALIDA(false);

    private final boolean entrada;

    TipoMovimiento(boolean entrada) {
        this.entrada = entrada;
    }

    public boolean esEntrada() {
        return entrada;
    }
}