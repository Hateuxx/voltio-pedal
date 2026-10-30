package com.voltiopedal.apiinventario.usuario;

import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
