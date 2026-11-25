package com.voltiopedal.apiinventario.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Integer> {

    /* Registros de una entidad desde una fecha en adelante, del más antiguo al más reciente.
       Sirve al reporte de eliminados: trae las bajas del rango y también las reactivaciones posteriores */
    @Query("SELECT r FROM RegistroAuditoria r JOIN FETCH r.usuario " +
           "WHERE r.entidadAfectada = :entidad AND r.fechaHora >= :desde " +
           "ORDER BY r.fechaHora ASC, r.idRegistroAuditoria ASC")
    List<RegistroAuditoria> listarDesde(@Param("entidad") String entidad, @Param("desde") LocalDateTime desde);

    /* Registros de un mismo código y acción, del más reciente al más antiguo (el primero es el último que ocurrió) */
    @Query("SELECT r FROM RegistroAuditoria r JOIN FETCH r.usuario " +
           "WHERE r.entidadAfectada = :entidad AND r.codigoRegistro = :codigo AND r.accion = :accion " +
           "ORDER BY r.fechaHora DESC, r.idRegistroAuditoria DESC")
    List<RegistroAuditoria> listarPorRegistroYAccion(@Param("entidad") String entidad,
                                                     @Param("codigo") String codigo,
                                                     @Param("accion") AccionAuditoria accion);
}