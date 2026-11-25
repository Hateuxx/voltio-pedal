package com.voltiopedal.apiinventario.serviciotecnico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServicioTecnicoRepository extends JpaRepository<ServicioTecnico, Integer> {

    /* ==============================
     *   CONSULTAS DE VARIAS TABLAS
     * ============================== */

    /* 1. Todos los servicios con su cliente, scooter y técnico (el técnico puede faltar: LEFT JOIN) */
    @Query("SELECT s FROM ServicioTecnico s JOIN FETCH s.cliente JOIN FETCH s.scooter " +
           "LEFT JOIN FETCH s.tecnico ORDER BY s.fechaIngreso DESC, s.idServicioTecnico DESC")
    List<ServicioTecnico> listarConRelaciones();

    /* 2. Un servicio por su id, con sus relaciones */
    @Query("SELECT s FROM ServicioTecnico s JOIN FETCH s.cliente JOIN FETCH s.scooter " +
           "LEFT JOIN FETCH s.tecnico WHERE s.idServicioTecnico = :id")
    Optional<ServicioTecnico> buscarPorIdConRelaciones(@Param("id") Integer id);

    /* 3. Servicios ingresados entre dos fechas (se usará en el reporte de servicio técnico) */
    @Query("SELECT s FROM ServicioTecnico s JOIN FETCH s.cliente JOIN FETCH s.scooter " +
           "LEFT JOIN FETCH s.tecnico WHERE s.fechaIngreso BETWEEN :inicio AND :fin " +
           "ORDER BY s.fechaIngreso DESC, s.idServicioTecnico DESC")
    List<ServicioTecnico> listarPorRangoConRelaciones(@Param("inicio") LocalDateTime inicio,
                                                      @Param("fin") LocalDateTime fin);

    /* ==============================
     *     CONSULTAS DE UNA TABLA
     * ============================== */

    /* Revisa si un equipo ya tiene un servicio en curso (distinto al estado indicado) */
    boolean existsByNumeroSerieAndEstadoNot(String numeroSerie, EstadoServicio estado);

    /* Cuántos servicios hay en un estado */
    long countByEstado(EstadoServicio estado);
}