package com.voltiopedal.apiinventario.inventario;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mercaderias")
public class KardexController {
    private final KardexService kardexService;

    public KardexController(KardexService kardexService) {
        this.kardexService = kardexService;
    }

    /* GET /api/mercaderias/{id}/kardex */
    @GetMapping("/{id}/kardex")
    public List<MovimientoResponse> kardex(@PathVariable Integer id) {
        return kardexService.listarPorMercaderia(id);
    }
}