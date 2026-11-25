package com.voltiopedal.apiinventario.venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    /* Ventas en un rango de fechas, de la más reciente a la más antigua */
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente " +
           "WHERE v.fechaHora >= :desde AND v.fechaHora < :hasta ORDER BY v.fechaHora DESC, v.idVenta DESC")
    List<Venta> buscarPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    /* Último número usado por tipo (los números tienen ancho fijo, así que MAX de texto sirve) */
    @Query("SELECT MAX(v.numeroComprobante) FROM Venta v WHERE v.tipoComprobante = :tipo")
    Optional<String> ultimoNumero(@Param("tipo") TipoComprobante tipo);
}
