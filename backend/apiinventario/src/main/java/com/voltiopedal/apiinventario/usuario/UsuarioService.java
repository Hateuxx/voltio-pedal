package com.voltiopedal.apiinventario.usuario;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service 
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /* Constructor para inyección de dependencias */
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional 
    public UsuarioResponse registrar(UsuarioRequest request) {
        String numeroDocumento = request.numeroDocumento().trim();

        /* Validar si ya existe el documento */
        if (usuarioRepository.existsByTipoDocumentoAndNumeroDocumento(request.tipoDocumento(), numeroDocumento)) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con este tipo y número de documento.");
        }

        /* Validar longitud exacta para el DNI */
        if (request.tipoDocumento() == TipoDocumento.DNI && !numeroDocumento.matches("^[0-9]{8}$")) {
            throw new ReglaNegocioException("El DNI debe tener exactamente 8 dígitos.");
        }

        Usuario usuario = new Usuario();
        usuario.setTipoDocumento(request.tipoDocumento());
        usuario.setNumeroDocumento(numeroDocumento);
        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setRol(request.rol());
        usuario.setPreguntaSeguridad(request.preguntaSeguridad().trim());

        /* Hashear la contraseña */
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));

        /* Normalizar y hashear la respuesta de seguridad */
        String respuestaNormalizada = request.respuestaSeguridad().trim().toLowerCase();
        usuario.setRespuestaSeguridad(passwordEncoder.encode(respuestaNormalizada));

        /* Guardar en base de datos */
        Usuario guardado = usuarioRepository.save(usuario);

        return aResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::aResponse).toList();
    }

    private Usuario obtenerPorId(Integer id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario con ID: " + id));
    }

    @Transactional 
    public void desactivar(Integer id, Integer idSolicitante) {
        Usuario usuario = obtenerPorId(id);

        /* Se usa.equals() para objetos Integer */
        if (id.equals(idSolicitante)) {
            throw new ReglaNegocioException("No puede desactivar su propia cuenta.");
        }

        /* Espejo de proveedor */
        if(!usuario.getActivo()) {
            throw new ReglaNegocioException("El usuario ya está desactivado.");
        }

        /* Verifica que no sea el último admin */
        if (usuario.getRol() == Rol.ADMINISTRADOR && usuarioRepository.countByRolAndActivoTrue(Rol.ADMINISTRADOR) <= 1) {
            throw new ReglaNegocioException("No se puede desactivar al último administrador activo.");
        }

        usuario.setActivo(false);
    }

    @Transactional 
    public void reactivar(Integer id) {
        Usuario usuario= obtenerPorId(id);
        if (usuario.getActivo()) {
            throw new ReglaNegocioException("El usuario ya está activo.");
        }
        usuario.setActivo(true);
    }

    private UsuarioResponse aResponse(Usuario u) {
        return new UsuarioResponse(
            u.getIdUsuario(),
            u.getTipoDocumento(),
            u.getNumeroDocumento(),
            u.getNombres(),
            u.getApellidos(),
            u.getRol(),
            u.getActivo()
        );
    }

}
