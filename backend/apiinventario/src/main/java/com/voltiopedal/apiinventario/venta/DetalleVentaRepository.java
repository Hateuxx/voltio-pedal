package com.voltiopedal.apiinventario.venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    /* ==============================
     *   CONSULTAS DE DOS TABLAS
     * ============================== */

    /* Detalles de una venta junto con su mercadería (JOIN), en el orden en que se registraron */
    @Query("SELECT d FROM DetalleVenta d JOIN FETCH d.mercaderia " +
           "WHERE d.venta.idVenta = :idVenta ORDER BY d.idDetalleVenta")
    List<DetalleVenta> listarPorVentaConMercaderia(@Param("idVenta") Integer idVenta);

    /* ==============================
     *     CONSULTAS DE UNA TABLA
     * ============================== */

    /* Cuántas líneas tiene una venta */
    long countByVentaIdVenta(Integer idVenta);

    /* Revisa si un producto ya se vendió alguna vez */
    boolean existsByMercaderiaIdMercaderia(Integer idMercaderia);

    /* Líneas de las ventas activas entre dos fechas, con su venta, cliente y producto (reporte de ventas en PDF) */
    @Query("SELECT d FROM DetalleVenta d JOIN FETCH d.venta v LEFT JOIN FETCH v.cliente JOIN FETCH d.mercaderia " +
           "WHERE v.activo = true AND v.fechaHora BETWEEN :inicio AND :fin " +
           "ORDER BY v.fechaHora DESC, v.idVenta DESC, d.idDetalleVenta")
    List<DetalleVenta> listarPorRangoConVentaYProducto(@Param("inicio") LocalDateTime inicio,
                                                       @Param("fin") LocalDateTime fin);
}