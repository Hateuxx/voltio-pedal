package com.voltiopedal.apiinventario.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component; 

@Component 
public class InicializadorAdmin implements CommandLineRunner{

    private static final Logger log = LoggerFactory.getLogger(InicializadorAdmin.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /* Lee las variables inyectadas desde application.yaml */
    @Value("${app.admin.numero-documento}")
    private String numeroDocumento;

    @Value("${app.admin.nombres}")
    private String nombres;

    @Value("${app.admin.apellidos}")
    private String apellidos;

    @Value("${app.admin.password}")
    private String password;

    @Value("${app.admin.pregunta-seguridad}")
    private String preguntaSeguridad;

    @Value("${app.admin.respuesta-seguridad}")
    private String respuestaSeguridad;

    /* Inyectar dependencias */
    public InicializadorAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override 
    public void run(String... args) {
        /* 1er control: Verificar si ya existe el rol ADMINISTRADOR si es así, cortamos la ejecución */
        if (usuarioRepository.existsByRol(Rol.ADMINISTRADOR)) {
            log.info("Ya existe un administrador en el sistema. No se crea la cuenta inicial");
            return;
        }

        /* 2do control agrupado: Evaluar si falta alguna de las variables */
        boolean faltaPassword = password == null || password.isBlank();
        boolean faltaRespuesta = respuestaSeguridad == null || respuestaSeguridad.isBlank();

        if (faltaPassword || faltaRespuesta) {
            StringBuilder errorMsg =  new StringBuilder("No se puede inicializar el sistema. Faltan las siguientes variables de entorno");
            if(faltaPassword) {
                errorMsg.append("\n - ADMIN_PASSWORD (contraseña del administrador)");            
            }
            if(faltaRespuesta) {
                errorMsg.append("\n - ADMIN_RESPUESTA (respuesta a la pregunta de seguridad)");
            }

            throw new IllegalStateException(errorMsg.toString());
        }

        /* Normalizar la respuesta de seguridad (minúscula) */
        String respuestaNormalizada = respuestaSeguridad.trim().toLowerCase();

        /* Creamos el usuario */
        Usuario admin = new Usuario();
        admin.setTipoDocumento(TipoDocumento.DNI); /* Asumido por DNI "70000001" */
        admin.setNumeroDocumento(numeroDocumento);
        admin.setNombres(nombres);
        admin.setApellidos(apellidos);
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setPreguntaSeguridad(preguntaSeguridad);

        /* Encriptar ambas credenciales sensibles con BCrypt */
        admin.setContrasena(passwordEncoder.encode(password));
        admin.setRespuestaSeguridad(passwordEncoder.encode(respuestaNormalizada));

        /* Nota: intentosFallidos (0) y activo (true) ya se asignan por defecto en la clase Usuario */
        /* Guardamos en la base de datos */
        usuarioRepository.save(admin);
        log.info("Administrador inicial creado exitosamente con el documento: {}", numeroDocumento);
    }
}
