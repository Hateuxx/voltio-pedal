package com.voltiopedal.apiinventario.venta;

import com.voltiopedal.apiinventario.common.MotivoRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /* POST /api/ventas: el cajero sale del token */
    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody VentaRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ventaService.registrar(request, Integer.valueOf(jwt.getSubject())));
    }

    /* GET /api/ventas?desde=2026-11-01&hasta=2026-11-30 (por defecto, últimos 30 días) */
    @GetMapping
    public List<VentaResponse> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ventaService.listar(desde, hasta);
    }

    @GetMapping("/{id}")
    public VentaResponse buscarPorId(@PathVariable Integer id) {
        return ventaService.buscarPorId(id);
    }

    /* POST /api/ventas/{id}/anular (solo admin, con motivo): devuelve el stock y deja auditoría */
    @PostMapping("/{id}/anular")
    public VentaResponse anular(@PathVariable Integer id, @Valid @RequestBody MotivoRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        return ventaService.anular(id, request.motivo(), Integer.valueOf(jwt.getSubject()));
    }
}
