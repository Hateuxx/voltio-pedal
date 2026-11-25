package com.voltiopedal.apiinventario.venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

     /* ==============================
     *     CONSULTAS DE UNA TABLA
     * ============================== */

    /* Busca una venta por tipo y número de comprobante */
    Optional<Venta> findByTipoComprobanteAndNumeroComprobante(TipoComprobante tipoComprobante, String numeroComprobante);

    /* Ventas activas entre dos fechas, de la más reciente a la más antigua (servirá para el reporte) */
    List<Venta> findByActivoTrueAndFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);

    /* ==============================
     *     CONSULTAS DE DOS TABLA
     * ============================== */

    /*Ventas activas con su cliente (LEFT JOIN), de la más reciente a la más antigua */
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente " +
           "WHERE v.activo = true ORDER BY v.fechaHora DESC")
    List<Venta> listarActivasConCliente();

    /*Una venta por su id, junto con su cliente (LEFT JOIN) */
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente " +
           "WHERE v.idVenta = :id")
    Optional<Venta> buscarPorIdConCliente(@Param("id") Integer id);

    /* 3. Ventas activas entre dos fechas, con su cliente (LEFT JOIN): se usa en el reporte de ventas */
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente " +
           "WHERE v.activo = true AND v.fechaHora BETWEEN :inicio AND :fin " +
           "ORDER BY v.fechaHora DESC")
    List<Venta> listarPorRangoConCliente(@Param("inicio") LocalDateTime inicio,
                                         @Param("fin") LocalDateTime fin);
}