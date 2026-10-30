package com.voltiopedal.apiinventario.ingresomercaderia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngresoMercaderiaRepository extends JpaRepository<IngresoMercaderia, Integer> {

    /* ==============================
     *   CONSULTAS DE DOS TABLAS
     * ============================== */

    /* 1. Ingresos activos con su proveedor (JOIN), del más reciente al más antiguo */
    @Query("SELECT i FROM IngresoMercaderia i JOIN FETCH i.proveedor " +
           "WHERE i.activo = true ORDER BY i.fechaHora DESC")
    List<IngresoMercaderia> listarActivosConProveedor();

    /* 2. Un ingreso por su id, junto con su proveedor (JOIN) */
    @Query("SELECT i FROM IngresoMercaderia i JOIN FETCH i.proveedor " +
           "WHERE i.idIngreso = :id")
    Optional<IngresoMercaderia> buscarPorIdConProveedor(@Param("id") Integer id);
}