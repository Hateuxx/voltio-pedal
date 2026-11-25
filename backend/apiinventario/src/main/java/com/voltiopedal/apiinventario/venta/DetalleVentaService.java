package com.voltiopedal.apiinventario.venta;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DetalleVentaService {
    private final DetalleVentaRepository detalleVentaRepository;
    private final VentaRepository ventaRepository;

    /* Insertar dependencias por constructor */
    public DetalleVentaService(DetalleVentaRepository detalleVentaRepository, VentaRepository ventaRepository) {
        this.detalleVentaRepository = detalleVentaRepository;
        this.ventaRepository = ventaRepository;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* Lista las líneas de una venta con los datos de cada producto */
    public List<DetalleVentaResponse> listarPorVenta(Integer idVenta) {
        /* Si la venta no existe, se responde 404 (y no una lista vacía engañosa) */
        if (!ventaRepository.existsById(idVenta)) {
            throw new RecursoNoEncontradoException("No se encontró ninguna venta con el id: " + idVenta);
        }
        return detalleVentaRepository.listarPorVentaConMercaderia(idVenta).stream().map(this::aResponse).toList();
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* MÉTODO PRIVADO: convierte Entidad -> Response */
    private DetalleVentaResponse aResponse(DetalleVenta d) {
        /* El subtotal no está en la base: se calcula como precio x cantidad */
        BigDecimal subtotal = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));

        return new DetalleVentaResponse(
                d.getIdDetalleVenta(),
                d.getVenta().getIdVenta(),
                d.getMercaderia().getIdMercaderia(),
                d.getMercaderia().getSku(),
                d.getMercaderia().getDescripcion(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getCostoUnitario(),
                subtotal,
                d.getNumeroSerie());
    }
}