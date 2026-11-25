package com.voltiopedal.apiinventario.mercaderia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;

import java.util.List;
import java.util.Optional;

public interface MercaderiaRepository extends JpaRepository<Mercaderia, Integer> {

    /* ==============================
     *   CONSULTAS DE DOS TABLAS
     * ============================== */

    /* 1. Mercadería activa con su proveedor (JOIN), ordenada por descripción */
    @Query("SELECT m FROM Mercaderia m JOIN FETCH m.proveedor " +
           "WHERE m.activo = true ORDER BY m.descripcion")
    List<Mercaderia> listarActivasConProveedor();

    /* 2. Una mercadería por su id, junto con su proveedor */
    @Query("SELECT m FROM Mercaderia m JOIN FETCH m.proveedor " +
           "WHERE m.idMercaderia = :id")
    Optional<Mercaderia> buscarPorIdConProveedor(@Param("id") Integer id);

       /* Todos los productos activos (se usa en los reportes para detectar productos sin movimiento) */
    List<Mercaderia> findByActivoTrue();

    /* ==============================
     *     CONSULTAS DE UNA TABLA
     * ============================== */

    /* Busca por SKU (se usará al registrar, en la Parte 2) */
    Optional<Mercaderia> findBySku(String sku);

    /* Revisa si otro producto ya usa ese SKU (se usará al actualizar, en la Parte 2) */
    boolean existsBySkuAndIdMercaderiaNot(String sku, Integer idMercaderia);

    /* Varios productos por sus SKU de una sola vez (el reporte de eliminados los cruza con la auditoría) */
    List<Mercaderia> findBySkuIn(Collection<String> skus);
}