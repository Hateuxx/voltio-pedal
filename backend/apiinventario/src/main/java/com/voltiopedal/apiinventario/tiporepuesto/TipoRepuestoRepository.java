package com.voltiopedal.apiinventario.tiporepuesto;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface TipoRepuestoRepository extends JpaRepository<TipoRepuesto, Integer>{

    /*Pregunta si ya existe un repuesto con ese nombre */
    boolean existsByNombre(String nombre);

    /*Metodo para listar por orden alfabetico */
    List<TipoRepuesto> findAllByOrderByNombreAsc();
}
