package com.voltiopedal.apiinventario.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    /* Buscar por tipo de documento y número de documento (retornando la "caja fuerte" Optional) */
    Optional<Usuario> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);

    /* Método extra muy útil para cuando implemente la creación de usuarios: */
    boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);

    /* Método para saber si ya existe algún usuario con un rol específico */
    boolean existsByRol(Rol rol);
}
