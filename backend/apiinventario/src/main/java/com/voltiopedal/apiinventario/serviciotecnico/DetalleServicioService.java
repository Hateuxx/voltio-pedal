package com.voltiopedal.apiinventario.serviciotecnico;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DetalleServicioService {
    private final DetalleServicioRepository detalleRepository;
    private final ServicioTecnicoRepository servicioRepository;

    /* Insertar dependencias por constructor */
    public DetalleServicioService(DetalleServicioRepository detalleRepository,
                                  ServicioTecnicoRepository servicioRepository) {
        this.detalleRepository = detalleRepository;
        this.servicioRepository = servicioRepository;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* Lista los repuestos usados en un servicio */
    public List<DetalleServicioResponse> listarPorServicio(Integer idServicio) {
        /* Si el servicio no existe, se responde 404 (y no una lista vacía engañosa) */
        if (!servicioRepository.existsById(idServicio)) {
            throw new RecursoNoEncontradoException("No se encontró ningún servicio técnico con el id: " + idServicio);
        }
        return detalleRepository.listarPorServicioConRepuesto(idServicio).stream().map(this::aResponse).toList();
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* MÉTODO PRIVADO: convierte Entidad -> Response */
    private DetalleServicioResponse aResponse(DetalleServicio d) {
        /* El subtotal no está en la base: se calcula como precio x cantidad */
        BigDecimal subtotal = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));

        return new DetalleServicioResponse(
                d.getIdDetalleServicio(),
                d.getServicioTecnico().getIdServicioTecnico(),
                d.getRepuesto().getIdMercaderia(),
                d.getRepuesto().getSku(),
                d.getRepuesto().getDescripcion(),
                d.getRepuesto().getTipoRepuesto().getNombre(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getCostoUnitario(),
                subtotal,
                d.getFechaHora());
    }
}
