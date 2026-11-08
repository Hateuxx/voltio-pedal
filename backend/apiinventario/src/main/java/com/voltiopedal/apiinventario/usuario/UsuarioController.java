package com.voltiopedal.apiinventario.usuario;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Inyección de dependencias por constructor
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse respuesta = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping 
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        Integer idSolicitante = Integer.valueOf(jwt.getSubject());
        usuarioService.desactivar(id, idSolicitante);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Integer id) {
        usuarioService.reactivar(id);
        return ResponseEntity.noContent().build();
    }
}
