package com.voltiopedal.apiinventario.mercaderia;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

public interface MercaderiaRepository extends JpaRepository<Mercaderia, Integer> {

    /* @EntityGraph trae el proveedor en la MISMA consulta (evita N+1 al mostrar la razón social) */
    @EntityGraph(attributePaths = "proveedor")
    List<Mercaderia> findByActivoTrueOrderByDescripcionAsc();

    @EntityGraph(attributePaths = "proveedor")
    List<Mercaderia> findByActivoFalseOrderByDescripcionAsc();

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdMercaderiaNot(String sku, Integer idMercaderia);

    /* SELECT ... FOR UPDATE: bloquea la fila hasta que termine la transacción (evita que dos ingresos pisen el stock) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mercaderia m WHERE m.idMercaderia = :id")
    Optional<Mercaderia> buscarParaActualizarStock(@Param("id") Integer id);

    /* Activos en o por debajo de su mínimo; los agotados primero */
    @Query("SELECT m FROM Mercaderia m JOIN FETCH m.proveedor " +
           "WHERE m.activo = true AND m.stock <= m.stockMinimo " +
           "ORDER BY m.stock ASC, m.descripcion ASC")
    List<Mercaderia> listarConStockBajo();
}
