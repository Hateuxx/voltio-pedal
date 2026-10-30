package com.voltiopedal.apiinventario.auth;

import com.voltiopedal.apiinventario.usuario.Usuario;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service 
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicioToken servicioToken;
    private final String dummyHash;

    private final int maxIntentos;
    private final Duration duracionBloqueo;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       ServicioToken servicioToken,
                       @Value("${app.seguridad.intentos-maximos}") int maxIntentos,
                       @Value("${app.seguridad.bloqueo}") Duration duracionBloqueo) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.servicioToken = servicioToken;
        this.maxIntentos = maxIntentos;
        this.duracionBloqueo = duracionBloqueo;
        /* Se genera en el constructor */
        this.dummyHash = passwordEncoder.encode("dummy-password-falso");
    }
    
    @Transactional(noRollbackFor = {CredencialesInvalidasException.class, CuentaBloqueadaException.class})
    public LoginResponse login(LoginRequest solicitud) {
        /* Buscar al usuario */
        var optUsuario = usuarioRepository.findByTipoDocumentoAndNumeroDocumento(solicitud.tipoDocumento(), solicitud.numeroDocumento());

        /* No existe o inactivo */
        if (optUsuario.isEmpty() || !optUsuario.get().getActivo()) {
            passwordEncoder.matches(solicitud.contrasena(), dummyHash);
            log.warn("Intento de login fallido para documento {} {}", solicitud.tipoDocumento(), solicitud.numeroDocumento());
            /* Ataque de tiempo mitigado */
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
        }

        Usuario usuario = optUsuario.get();
        LocalDateTime ahora = LocalDateTime.now();

        /* Bloqueado actualmente */
        if (usuario.getBloqueadoHasta() != null && ahora.isBefore(usuario.getBloqueadoHasta())) {
            log.warn("Intento de ingreso a cuenta bloqueada para documento {} {}", solicitud.tipoDocumento(), solicitud.numeroDocumento());
            throw new CuentaBloqueadaException("Cuenta bloqueada temporalmente.", usuario.getBloqueadoHasta().atZone(ZoneId.systemDefault()).toInstant());
        }

        /* Bloqueo ya expiró */
        if (usuario.getBloqueadoHasta() != null && !ahora.isBefore(usuario.getBloqueadoHasta())) {
            usuario.setIntentosFallidos((byte) 0);
            usuario.setBloqueadoHasta(null);
        }

        /* Contraseña incorrecta */
        if (!passwordEncoder.matches(solicitud.contrasena(), usuario.getContrasena())) {
            byte nuevoIntentos = (byte) (usuario.getIntentosFallidos() + 1);
            usuario.setIntentosFallidos(nuevoIntentos);
            
            if (nuevoIntentos >= maxIntentos) {
                usuario.setBloqueadoHasta(ahora.plus(duracionBloqueo));
                log.warn("Cuenta alcanzó el limite de intentos y fue bloqueada: {} {}", solicitud.tipoDocumento(), solicitud.numeroDocumento());
                throw new CuentaBloqueadaException("Cuenta bloqueada temporalmente por demasiados intentos fallidos", usuario.getBloqueadoHasta().atZone(ZoneId.systemDefault()).toInstant());
            }
            log.warn("Intento de login fallido (intento {}/{}) para documento {} {}", nuevoIntentos, maxIntentos, solicitud.tipoDocumento(), solicitud.numeroDocumento());
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
        }

        /* Contraseña correcta */
        usuario.setIntentosFallidos((byte) 0);
        usuario.setBloqueadoHasta(null);

        var tokenData = servicioToken.crearToken(usuario);

        return new LoginResponse(
                tokenData.token(),
                tokenData.vence(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getRol());
    }
}
