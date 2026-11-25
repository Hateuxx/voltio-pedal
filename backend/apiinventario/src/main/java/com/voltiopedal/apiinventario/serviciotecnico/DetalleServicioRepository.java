package com.voltiopedal.apiinventario.serviciotecnico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetalleServicioRepository extends JpaRepository<DetalleServicio, Integer> {

    /* ==============================
     *   CONSULTAS DE DOS TABLAS
     * ============================== */

    /* Repuestos usados en un servicio, junto con los datos del repuesto (JOIN), en el orden en que se registraron */
    @Query("SELECT d FROM DetalleServicio d JOIN FETCH d.repuesto " +
           "WHERE d.servicioTecnico.idServicioTecnico = :idServicio " +
           "ORDER BY d.fechaHora, d.idDetalleServicio")
    List<DetalleServicio> listarPorServicioConRepuesto(@Param("idServicio") Integer idServicio);

    /* ==============================
     *     CONSULTAS DE UNA TABLA
     * ============================== */

    /* Cuántos repuestos distintos se usaron en un servicio */
    long countByServicioTecnicoIdServicioTecnico(Integer idServicioTecnico);

    /* Revisa si un repuesto ya se usó alguna vez en un servicio */
    boolean existsByRepuestoIdMercaderia(Integer idMercaderia);
}