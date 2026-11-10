package com.voltiopedal.apiinventario.auth;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
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
    
}
