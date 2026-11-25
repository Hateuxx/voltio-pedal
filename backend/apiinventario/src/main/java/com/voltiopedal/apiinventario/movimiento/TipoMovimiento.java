package com.voltiopedal.apiinventario.movimiento;

public enum TipoMovimiento {
    ENTRADA_PROVEEDOR(true),
    DEVOLUCION_PROVEEDOR(false),
    SALIDA_VENTA(false),
    DEVOLUCION_VENTA(true),
    SALIDA_REPARACION(false),
    DEVOLUCION_REPARACION(true),
    AJUSTE_ENTRADA(true),
    AJUSTE_SALIDA(false);

    /* true si el movimiento suma al stock, false si resta (igual que la regla de la base de datos) */
    private final boolean aumentaStock;

    TipoMovimiento(boolean aumentaStock) {
        this.aumentaStock = aumentaStock;
    }

    public boolean aumentaStock() {
        return aumentaStock;
    }
}