package com.voltiopedal.apiinventario.tiporepuesto;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface TipoRepuestoRepository extends JpaRepository<TipoRepuesto, Integer>{

    /* ==============================
     *          CONSULTAS
     * ============================== */

    /*Metodo para listar por orden alfabetico */
    List<TipoRepuesto> findAllByOrderByNombreAsc();

    /* ==============================
     *   VALIDACIONES DE DUPLICADO
     * ============================== */

    /*Pregunta si ya existe un repuesto con ese nombre */
    boolean existsByNombre(String nombre);

    /* Verifica si OTRO tipo (con id distinto) ya usa ese nombre */
    boolean existsByNombreAndIdTipoRepuestoNot(String nombre, Integer id);
}