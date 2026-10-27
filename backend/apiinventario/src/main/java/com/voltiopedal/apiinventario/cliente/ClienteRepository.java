package com.voltiopedal.apiinventario.cliente;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer>{

    /* Pregunta si ya existe un cliente con ese tipo y número de documento */
    boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumentoCliente tipoDocumento, String numeroDocumento);

    /*Busca un cliente por tipo y número de documento */
    Optional<Cliente> findByTipoDocumentoAndNumeroDocumento( TipoDocumentoCliente tipoDocumento, String numeroDocumento);
    
    /* Lista todos los clientes ordenados alfabéticamente por nombres*/
    List<Cliente> findAllByOrderByNombresAsc();


}
