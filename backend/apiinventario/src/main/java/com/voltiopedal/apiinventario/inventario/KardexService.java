package com.voltiopedal.apiinventario.inventario;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* ÚNICO lugar del sistema que cambia el stock. Todo movimiento deja su fila en el kardex */
@Service
public class KardexService {
    private final MovimientoInventarioRepository movimientoRepository;
    private final MercaderiaRepository mercaderiaRepository;
    private final UsuarioRepository usuarioRepository;

    public KardexService(MovimientoInventarioRepository movimientoRepository,
            MercaderiaRepository mercaderiaRepository,
            UsuarioRepository usuarioRepository) {
        this.movimientoRepository = movimientoRepository;
        this.mercaderiaRepository = mercaderiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /*
     * MANDATORY: exige que quien llama ya tenga una transacción abierta (si no,
     * lanza error)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public MovimientoInventario registrarEntradaPorIngreso(Mercaderia mercaderia, int cantidad,
            Integer idUsuario, Integer idIngreso) {
        MovimientoInventario mov = nuevo(mercaderia, TipoMovimiento.ENTRADA_PROVEEDOR, cantidad, idUsuario);
        mov.setIdIngreso(idIngreso);
        return aplicar(mov);
    }

    /* Ajuste por conteo físico: el signo de la diferencia decide si suma o resta */
    @Transactional(propagation = Propagation.MANDATORY)
    public MovimientoInventario registrarAjustePorConteo(Mercaderia mercaderia, int diferencia, Integer idUsuario,
                                                         Integer idConteo, String observacion) {
        TipoMovimiento tipo = diferencia > 0 ? TipoMovimiento.AJUSTE_ENTRADA : TipoMovimiento.AJUSTE_SALIDA;
        MovimientoInventario mov = nuevo(mercaderia, tipo, Math.abs(diferencia), idUsuario);
        mov.setIdConteo(idConteo);
        mov.setObservacion(observacion);     /* obligatorio en ajustes (CHECK ck_mov_ajuste_motivo) */
        return aplicar(mov);
    }

    /*
     * ==============================
     * CONSULTA DEL KARDEX
     * ==============================
     */

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarPorMercaderia(Integer idMercaderia) {
        if (!mercaderiaRepository.existsById(idMercaderia)) {
            throw new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + idMercaderia);
        }

        List<MovimientoInventario> movimientos = movimientoRepository
                .findByMercaderia_IdMercaderiaOrderByFechaHoraAscIdMovimientoAsc(idMercaderia);

        /* UNA sola consulta para todos los usuarios involucrados (evita N+1) */
        Set<Integer> idsUsuario = movimientos.stream().map(mov -> mov.getIdUsuario()).collect(Collectors.toSet());
        Map<Integer, String> nombres = usuarioRepository.findAllById(idsUsuario).stream()
                .collect(Collectors.toMap(u -> u.getIdUsuario(), u -> u.getNombres() + " " + u.getApellidos()));

        return movimientos.stream()
                .map(mov -> new MovimientoResponse(
                        mov.getIdMovimiento(),
                        mov.getFechaHora(),
                        mov.getTipo(),
                        mov.getTipo().esEntrada(),
                        mov.getCantidad(),
                        mov.getStockAnterior(),
                        mov.getStockResultante(),
                        nombres.getOrDefault(mov.getIdUsuario(), "Usuario #" + mov.getIdUsuario()),
                        origenDe(mov),
                        mov.getObservacion()))
                .toList();
    }

    private String origenDe(MovimientoInventario mov) {
        if (mov.getIdIngreso() != null)
            return "Ingreso #" + mov.getIdIngreso();
        if (mov.getIdVenta() != null)
            return "Venta #" + mov.getIdVenta();
        if (mov.getIdServicioTecnico() != null)
            return "Servicio técnico #" + mov.getIdServicioTecnico();
        if (mov.getIdConteo() != null)
            return "Conteo #" + mov.getIdConteo();
        return "Ajuste manual";
    }

    /*
     * (Aquí se agregarán registrarSalidaPorVenta, registrarSalidaPorReparacion,
     * etc.)
     */

    private MovimientoInventario nuevo(Mercaderia mercaderia, TipoMovimiento tipo, int cantidad, Integer idUsuario) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setMercaderia(mercaderia);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setIdUsuario(idUsuario);
        mov.setFechaHora(LocalDateTime.now());
        return mov;
    }

    /*
     * Núcleo: calcula el saldo, valida que no quede negativo y actualiza el stock
     */
    private MovimientoInventario aplicar(MovimientoInventario mov) {
        Mercaderia m = mov.getMercaderia();
        int anterior = m.getStock();
        int resultante = mov.getTipo().esEntrada()
                ? anterior + mov.getCantidad()
                : anterior - mov.getCantidad();

        if (resultante < 0) {
            throw new ReglaNegocioException("Stock insuficiente de " + m.getSku() + ": hay " + anterior
                    + " y se requieren " + mov.getCantidad() + ".");
        }

        mov.setStockAnterior(anterior);
        mov.setStockResultante(resultante);
        m.setStock(resultante); /* dirty checking: se guarda al cerrar la transacción */
        return movimientoRepository.save(mov);
    }
}