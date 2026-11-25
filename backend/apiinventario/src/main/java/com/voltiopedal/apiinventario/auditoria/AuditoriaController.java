package com.voltiopedal.apiinventario.auditoria;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /* GET /api/auditoria?entidad=PROVEEDOR&accion=INACTIVACION (ambos opcionales) */
    @GetMapping
    public List<AuditoriaResponse> listar(@RequestParam(required = false) String entidad,
                                          @RequestParam(required = false) AccionAuditoria accion) {
        return auditoriaService.listar(entidad, accion);
    }
}