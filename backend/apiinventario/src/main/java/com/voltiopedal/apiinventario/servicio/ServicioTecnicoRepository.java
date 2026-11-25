package com.voltiopedal.apiinventario.servicio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ServicioTecnicoRepository extends JpaRepository<ServicioTecnico, Integer> {

    /* Estado opcional: si llega null, lista todos */
    @Query("SELECT s FROM ServicioTecnico s JOIN FETCH s.cliente JOIN FETCH s.scooter " +
           "WHERE (:estado IS NULL OR s.estado = :estado) ORDER BY s.fechaIngreso DESC, s.idServicioTecnico DESC")
    List<ServicioTecnico> buscar(@Param("estado") EstadoServicio estado);

    @Query("SELECT s FROM ServicioTecnico s JOIN FETCH s.cliente JOIN FETCH s.scooter " +
           "WHERE s.fechaIngreso >= :desde AND s.fechaIngreso < :hasta ORDER BY s.fechaIngreso DESC")
    List<ServicioTecnico> buscarPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    /* Evita abrir dos órdenes para el mismo scooter mientras una sigue en taller */
    boolean existsByNumeroSerieAndEstadoNot(String numeroSerie, EstadoServicio estado);
}
