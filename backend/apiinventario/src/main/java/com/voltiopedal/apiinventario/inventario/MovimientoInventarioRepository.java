package com.voltiopedal.apiinventario.inventario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    /* Kardex de un producto en orden cronológico (el id desempata movimientos del mismo segundo) */
    List<MovimientoInventario> findByMercaderia_IdMercaderiaOrderByFechaHoraAscIdMovimientoAsc(Integer idMercaderia);
}