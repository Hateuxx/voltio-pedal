package com.voltiopedal.apiinventario.inventario;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import com.voltiopedal.apiinventario.venta.VentaRepository;

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
    private final VentaRepository ventaRepository;

    public KardexService(MovimientoInventarioRepository movimientoRepository,
            MercaderiaRepository mercaderiaRepository,
            UsuarioRepository usuarioRepository,
            VentaRepository ventaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.mercaderiaRepository = mercaderiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
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

    /* Salida por venta (RF11) */
    @Transactional(propagation = Propagation.MANDATORY)
    public MovimientoInventario registrarSalidaPorVenta(Mercaderia mercaderia, int cantidad,
                                                       Integer idUsuario, Integer idVenta) {
        MovimientoInventario mov = nuevo(mercaderia, TipoMovimiento.SALIDA_VENTA, cantidad, idUsuario);
        mov.setIdVenta(idVenta);
        return aplicar(mov);
    }

    /* Devolución al anular una venta: el stock vuelve a entrar */
    @Transactional(propagation = Propagation.MANDATORY)
    public MovimientoInventario registrarDevolucionPorVenta(Mercaderia mercaderia, int cantidad,
                                                           Integer idUsuario, Integer idVenta, String observacion) {
        MovimientoInventario mov = nuevo(mercaderia, TipoMovimiento.DEVOLUCION_VENTA, cantidad, idUsuario);
        mov.setIdVenta(idVenta);
        mov.setObservacion(observacion);
        return aplicar(mov);
    }

    /* Salida de repuestos consumidos en un servicio técnico (RF17) */
    @Transactional(propagation = Propagation.MANDATORY)
    public MovimientoInventario registrarSalidaPorReparacion(Mercaderia mercaderia, int cantidad,
                                                            Integer idUsuario, Integer idServicio) {
        MovimientoInventario mov = nuevo(mercaderia, TipoMovimiento.SALIDA_REPARACION, cantidad, idUsuario);
        mov.setIdServicioTecnico(idServicio);
        return aplicar(mov);
    }

    /* Devolución de un repuesto no usado en un servicio técnico */
    @Transactional(propagation = Propagation.MANDATORY)
    public MovimientoInventario registrarDevolucionPorReparacion(Mercaderia mercaderia, int cantidad,
                                                                Integer idUsuario, Integer idServicio) {
        MovimientoInventario mov = nuevo(mercaderia, TipoMovimiento.DEVOLUCION_REPARACION, cantidad, idUsuario);
        mov.setIdServicioTecnico(idServicio);
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

        /* Igual para las ventas: el kardex muestra el N.º de comprobante (B001-...), no el id interno */
        Set<Integer> idsVenta = movimientos.stream().map(mov -> mov.getIdVenta()).filter(id -> id != null).collect(Collectors.toSet());
        Map<Integer, String> comprobantes = ventaRepository.findAllById(idsVenta).stream()
                .collect(Collectors.toMap(v -> v.getIdVenta(), v -> v.getNumeroComprobante()));

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
                        origenDe(mov, comprobantes),
                        mov.getObservacion()))
                .toList();
    }

    private String origenDe(MovimientoInventario mov, Map<Integer, String> comprobantes) {
        if (mov.getIdIngreso() != null)
            return "Ingreso #" + mov.getIdIngreso();
        if (mov.getIdVenta() != null)
            return "Venta " + comprobantes.getOrDefault(mov.getIdVenta(), "#" + mov.getIdVenta());
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