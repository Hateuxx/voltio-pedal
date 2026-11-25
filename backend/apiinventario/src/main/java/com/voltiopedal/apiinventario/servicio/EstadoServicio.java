package com.voltiopedal.apiinventario.servicio;

/* Espejo del CHECK ck_servicio_estado (flujo de la orden de servicio) */
public enum EstadoServicio {
    INGRESADO,
    EN_REPARACION,
    ESPERANDO_REPUESTO,
    LISTO_PARA_ENTREGA,
    ENTREGADO
}
