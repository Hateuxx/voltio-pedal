package com.voltiopedal.apiinventario.auth;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return authService.login(solicitud);
    }
    
    @PostMapping("/recuperar/pregunta")
    public ResponseEntity<PreguntaResponse> obtenerPregunta(@Valid @RequestBody PreguntaRequest solicitud) {
        return ResponseEntity.ok(authService.obtenerPregunta(solicitud));
    }

    @PostMapping("/recuperar/restablecer")
    public ResponseEntity<Void> restablecer(@Valid @RequestBody RestablecerRequest solicitud) {
        authService.restablecer(solicitud);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cambiar-contrasena")
    public ResponseEntity<Void> cambiarContrasena(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CambiarContrasenaRequest solicitud) {
        Integer idUsuario = Integer.valueOf(jwt.getSubject());
        authService.cambiarContrasena(idUsuario, solicitud);
        return ResponseEntity.noContent().build();
    }
    
}
