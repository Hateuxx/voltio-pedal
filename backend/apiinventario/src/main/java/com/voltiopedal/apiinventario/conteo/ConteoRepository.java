package com.voltiopedal.apiinventario.conteo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConteoRepository extends JpaRepository<ConteoInventario, Integer> {

    boolean existsByEstado(EstadoConteo estado);

    List<ConteoInventario> findAllByOrderByFechaInicioDesc();
}