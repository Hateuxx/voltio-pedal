package com.voltiopedal.apiinventario.mercaderia;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MercaderiaRepository extends JpaRepository<Mercaderia, Integer> {

    /* @EntityGraph trae el proveedor en la MISMA consulta (evita N+1 al mostrar la razón social) */
    @EntityGraph(attributePaths = "proveedor")
    List<Mercaderia> findByActivoTrueOrderByDescripcionAsc();

    @EntityGraph(attributePaths = "proveedor")
    List<Mercaderia> findByActivoFalseOrderByDescripcionAsc();

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdMercaderiaNot(String sku, Integer idMercaderia);
}
