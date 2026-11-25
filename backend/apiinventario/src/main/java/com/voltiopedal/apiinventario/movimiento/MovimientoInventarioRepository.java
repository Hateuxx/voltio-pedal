package com.voltiopedal.apiinventario.movimiento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    /* ==============================
     *   CONSULTAS DE DOS TABLAS
     * ============================== */

    /* 1. Todos los movimientos con su producto (JOIN), del más reciente al más antiguo */
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.mercaderia " +
           "ORDER BY m.fechaHora DESC, m.idMovimiento DESC")
    List<MovimientoInventario> listarConMercaderia();

    /* 2. Un movimiento por su id, junto con su producto */
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.mercaderia " +
           "WHERE m.idMovimiento = :id")
    Optional<MovimientoInventario> buscarPorIdConMercaderia(@Param("id") Integer id);

    /* 3. Kardex: movimientos de un solo producto, del más reciente al más antiguo */
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.mercaderia " +
           "WHERE m.mercaderia.idMercaderia = :idMercaderia " +
           "ORDER BY m.fechaHora DESC, m.idMovimiento DESC")
    List<MovimientoInventario> listarPorMercaderiaConMercaderia(@Param("idMercaderia") Integer idMercaderia);

    /* 4. Movimientos entre dos fechas (se usará en el reporte de stock) */
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.mercaderia " +
           "WHERE m.fechaHora BETWEEN :inicio AND :fin " +
           "ORDER BY m.fechaHora DESC, m.idMovimiento DESC")
    List<MovimientoInventario> listarPorRangoConMercaderia(@Param("inicio") LocalDateTime inicio,
                                                           @Param("fin") LocalDateTime fin);

    /* Movimientos desde una fecha en adelante (sin límite superior), del más antiguo al más reciente.*/
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.mercaderia " +
           "WHERE m.fechaHora >= :inicio " +
           "ORDER BY m.fechaHora ASC, m.idMovimiento ASC")
    List<MovimientoInventario> listarDesdeConMercaderia(@Param("inicio") LocalDateTime inicio);

    /* ==============================
     *     CONSULTAS DE UNA TABLA
     * ============================== */

    /* Cuántos movimientos tiene un producto */
    long countByMercaderiaIdMercaderia(Integer idMercaderia);

    /* Cuántos movimientos hay de un tipo */
    long countByTipo(TipoMovimiento tipo);

    
}
