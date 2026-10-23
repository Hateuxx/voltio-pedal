package com.voltiopedal.apiinventario.proveedor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer>{
    /* 1. Busca el proveedor por su RUC */
    /* Con optional es como abrir la caja antes de usar lo que contiene */
    Optional<Proveedor> findByRuc(String ruc);

    /* 2. Verifica si ya existe un RUC */
    boolean existsByRuc(String ruc);

    /* 3. Listar solo los proveedores activos */
    List<Proveedor> findByActivoTrue();
}
