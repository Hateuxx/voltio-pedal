package com.voltiopedal.apiinventario.movimiento;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimientoInventarioService {
    private final MovimientoInventarioRepository movimientoRepository;
    private final MercaderiaRepository mercaderiaRepository;

    /* Insertar dependencias por constructor */
    public MovimientoInventarioService(MovimientoInventarioRepository movimientoRepository,
                                       MercaderiaRepository mercaderiaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.mercaderiaRepository = mercaderiaRepository;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* Lista todos los movimientos, del más reciente al más antiguo */
    public List<MovimientoInventarioResponse> listar() {
        return movimientoRepository.listarConMercaderia().stream().map(this::aResponse).toList();
    }

    /* Busca un movimiento por su id */
    public MovimientoInventarioResponse buscarPorId(Integer id) {
        MovimientoInventario movimiento = movimientoRepository.buscarPorIdConMercaderia(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún movimiento con el id: " + id));
        return aResponse(movimiento);
    }

    /* Kardex: lista los movimientos de un producto */
    public List<MovimientoInventarioResponse> listarPorMercaderia(Integer idMercaderia) {
        /* Si el producto no existe, se responde 404 (y no una lista vacía engañosa) */
        if (!mercaderiaRepository.existsById(idMercaderia)) {
            throw new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + idMercaderia);
        }
        return movimientoRepository.listarPorMercaderiaConMercaderia(idMercaderia).stream().map(this::aResponse).toList();
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* MÉTODO PRIVADO: convierte Entidad -> Response */
    private MovimientoInventarioResponse aResponse(MovimientoInventario m) {
        return new MovimientoInventarioResponse(
                m.getIdMovimiento(),
                m.getFechaHora(),
                m.getTipo(),
                m.getTipo().aumentaStock(),
                m.getCantidad(),
                m.getStockAnterior(),
                m.getStockResultante(),
                m.getMercaderia().getIdMercaderia(),
                m.getMercaderia().getSku(),
                m.getMercaderia().getDescripcion(),
                m.getIdUsuario(),
                m.getIdVenta(),
                m.getIdServicioTecnico(),
                m.getIdIngreso(),
                m.getIdConteo(),
                m.getObservacion());
    }
}