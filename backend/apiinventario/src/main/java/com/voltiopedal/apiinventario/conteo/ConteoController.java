package com.voltiopedal.apiinventario.conteo;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conteos")
public class ConteoController {
    private final ConteoService conteoService;

    public ConteoController(ConteoService conteoService) {
        this.conteoService = conteoService;
    }

    @GetMapping
    public List<ConteoResponse> listar() {
        return conteoService.listar();
    }

    @GetMapping("/{id}")
    public ConteoResponse buscarPorId(@PathVariable Integer id) {
        return conteoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ConteoResponse> abrir(@Valid @RequestBody ConteoRequests.Abrir request,
                                                @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conteoService.abrir(request, Integer.valueOf(jwt.getSubject())));
    }

    @PutMapping("/{id}/lineas")
    public ConteoResponse registrarLinea(@PathVariable Integer id, @Valid @RequestBody ConteoRequests.Linea request) {
        return conteoService.registrarLinea(id, request);
    }

    @DeleteMapping("/{id}/lineas/{idMercaderia}")
    public ConteoResponse quitarLinea(@PathVariable Integer id, @PathVariable Integer idMercaderia) {
        return conteoService.quitarLinea(id, idMercaderia);
    }

    @PostMapping("/{id}/cerrar")
    public ConteoResponse cerrar(@PathVariable Integer id, @Valid @RequestBody ConteoRequests.Cerrar request,
                                 @AuthenticationPrincipal Jwt jwt) {
        return conteoService.cerrar(id, request, Integer.valueOf(jwt.getSubject()));
    }
}