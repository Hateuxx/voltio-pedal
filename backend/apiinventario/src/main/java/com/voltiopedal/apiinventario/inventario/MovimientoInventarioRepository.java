package com.voltiopedal.apiinventario.inventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    /* Kardex de un producto en orden cronológico (el id desempata movimientos del mismo segundo) */
    List<MovimientoInventario> findByMercaderia_IdMercaderiaOrderByFechaHoraAscIdMovimientoAsc(Integer idMercaderia);

    /* Movimientos de todo el almacén en un rango (reporte RF14); tipo opcional */
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.mercaderia " +
           "WHERE m.fechaHora >= :desde AND m.fechaHora < :hasta AND (:tipo IS NULL OR m.tipo = :tipo) " +
           "ORDER BY m.fechaHora DESC, m.idMovimiento DESC")
    List<MovimientoInventario> buscarPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                              @Param("tipo") TipoMovimiento tipo);
}
