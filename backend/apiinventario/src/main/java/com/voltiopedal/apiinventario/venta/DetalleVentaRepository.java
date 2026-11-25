package com.voltiopedal.apiinventario.venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    boolean existsByNumeroSerie(String numeroSerie);

    /* Para la garantía del servicio técnico: la venta ACTIVA de un scooter por su número de serie */
    @Query("SELECT d FROM DetalleVenta d JOIN FETCH d.venta v WHERE d.numeroSerie = :serie AND v.activo = true")
    Optional<DetalleVenta> buscarVentaActivaPorSerie(@Param("serie") String numeroSerie);
}
