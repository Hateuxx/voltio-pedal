package com.voltiopedal.apiinventario.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Integer> {

    /* Filtros opcionales: si llega null, esa condición se ignora */
    @Query("SELECT r FROM RegistroAuditoria r " +
           "WHERE (:entidad IS NULL OR r.entidadAfectada = :entidad) " +
           "AND (:accion IS NULL OR r.accion = :accion) " +
           "ORDER BY r.fechaHora DESC, r.idRegistroAuditoria DESC")
    List<RegistroAuditoria> buscar(@Param("entidad") String entidad, @Param("accion") AccionAuditoria accion);
}