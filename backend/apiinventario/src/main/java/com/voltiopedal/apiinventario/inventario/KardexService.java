package com.voltiopedal.apiinventario.inventario;

import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/* ÚNICO lugar del sistema que cambia el stock. Todo movimiento deja su fila en el kardex */
@Service
public class KardexService {
    private final MovimientoInventarioRepository movimientoRepository;

    public KardexService(MovimientoInventarioRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    /* MANDATORY: exige que quien llama ya tenga una transacción abierta (si no, lanza error) */
    @Transactional(propagation = Propagation.MANDATORY)
    public MovimientoInventario registrarEntradaPorIngreso(Mercaderia mercaderia, int cantidad,
                                                            Integer idUsuario, Integer idIngreso) {
        MovimientoInventario mov = nuevo(mercaderia, TipoMovimiento.ENTRADA_PROVEEDOR, cantidad, idUsuario);
        mov.setIdIngreso(idIngreso);
        return aplicar(mov);
    }

    /* (Aquí se agregarán registrarSalidaPorVenta, registrarSalidaPorReparacion, etc.) */

    private MovimientoInventario nuevo(Mercaderia mercaderia, TipoMovimiento tipo, int cantidad, Integer idUsuario) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setMercaderia(mercaderia);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setIdUsuario(idUsuario);
        mov.setFechaHora(LocalDateTime.now());
        return mov;
    }

    /* Núcleo: calcula el saldo, valida que no quede negativo y actualiza el stock */
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
        m.setStock(resultante);          /* dirty checking: se guarda al cerrar la transacción */
        return movimientoRepository.save(mov);
    }
}