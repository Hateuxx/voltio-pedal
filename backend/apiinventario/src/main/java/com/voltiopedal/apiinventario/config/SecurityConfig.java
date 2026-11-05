package com.voltiopedal.apiinventario.config;

import com.voltiopedal.apiinventario.usuario.Rol;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/* Seguridad de la API: valida el token JWT en cada petición y aplica los permisos por rol */
@Configuration 
public class SecurityConfig {

    /* Nombres de los roles, tomados del enum Rol para evitar errores de escritura */
    private static final String ADMIN = Rol.ADMINISTRADOR.name();
    private static final String OPERADOR = Rol.OPERADOR_TIENDA.name();
    private static final String TECNICO = Rol.TECNICO.name();

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            /* CSRF se desactiva: la API no usa cookies ni sesión, solo token en cada petición */
            .csrf(csrf -> csrf.disable())
            /* Sin sesión en el servidor: cada petición debe traer su token */
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            /* Reglas de acceso: se evalúan en orden y gana la primera que coincide */
            .authorizeHttpRequests(auth -> auth
                /* Rutas públicas: login, documentación Swagger y página de error */
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()

                /* Añadir /actuator/health a la lista de permitAll() */
                .requestMatchers("/actuator/health").permitAll()

                /* Usuarios: solo el administrador */
                .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole(ADMIN)

                /* Clientes: consultar (todos), registrar (admin y operador), lo demás solo admin */
                .requestMatchers(HttpMethod.GET, "/api/clientes", "/api/clientes/**").hasAnyRole(ADMIN, OPERADOR, TECNICO)
                .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole(ADMIN, OPERADOR)
                .requestMatchers("/api/clientes", "/api/clientes/**").hasRole(ADMIN)

                /* Inactivos: solo el administrador (debe ir ANTES de la regla GET general) */
                .requestMatchers(HttpMethod.GET, "/api/proveedores/inactivos").hasRole(ADMIN)

                /* Proveedores: consultar (admin y operador), lo demás solo admin */
                .requestMatchers(HttpMethod.GET, "/api/proveedores", "/api/proveedores/**").hasAnyRole(ADMIN, OPERADOR)
                .requestMatchers("/api/proveedores", "/api/proveedores/**").hasRole(ADMIN)

                /* Tipos de repuesto: consultar (todos), lo demás solo admin */
                .requestMatchers(HttpMethod.GET, "/api/tipos-repuesto", "/api/tipos-repuesto/**").hasAnyRole(ADMIN, OPERADOR, TECNICO)
                .requestMatchers("/api/tipos-repuesto", "/api/tipos-repuesto/**").hasRole(ADMIN)

                /* Regla específica POST antes de la regla general */
                /* Se habilita POST para Operador y Admin en preparación para el futuro módulo de ingresos */
                .requestMatchers(HttpMethod.POST, "/api/ingresos").hasAnyRole(ADMIN, OPERADOR)

                /* Ingresos de mercadería: consultar (admin y operador), lo demás solo admin */
                .requestMatchers(HttpMethod.GET, "/api/ingresos", "/api/ingresos/**").hasAnyRole(ADMIN, OPERADOR)
                .requestMatchers("/api/ingresos", "/api/ingresos/**").hasRole(ADMIN)

                /* Cualquier otra ruta: basta con haber iniciado sesión */
                .anyRequest().authenticated())
            /* Activa la validación del token JWT y la lectura del rol */
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorJwt()))
                .authenticationEntryPoint(sinCredenciales())
                .accessDeniedHandler(sinPermiso()))
            /* Respuestas de error en JSON cuando falta el token o el rol no alcanza */
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(sinCredenciales())
                .accessDeniedHandler(sinPermiso()));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* Convierte el dato "rol" del token en un permiso que Spring entiende (ROLE_ADMINISTRADOR, etc.) */
    private JwtAuthenticationConverter convertidorJwt() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    /* 401: no hay token, es inválido o está vencido */
    private AuthenticationEntryPoint sinCredenciales() {
        return (request, response, ex) -> responderError(response, HttpServletResponse.SC_UNAUTHORIZED,
                "No autenticado", "Debe iniciar sesión con un token válido.");
    }

    /* 403: el token es válido, pero el rol no tiene permiso para esta operación */
    private AccessDeniedHandler sinPermiso() {
        return (request, response, ex) -> responderError(response, HttpServletResponse.SC_FORBIDDEN,
                "Acceso denegado", "Su rol no tiene permiso para realizar esta operación.");
    }

    /* Escribe el error en JSON, con el mismo estilo (status, title, detail) del resto de la API */
    private void responderError(HttpServletResponse response, int estado, String titulo, String detalle) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + estado + ",\"title\":\"" + titulo + "\",\"detail\":\"" + detalle + "\"}");
    }
}

