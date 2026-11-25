package com.voltiopedal.apiinventario.movimiento;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoInventarioController {
    private final MovimientoInventarioService movimientoService;

    /* Insertar dependencia por constructor */
    public MovimientoInventarioController(MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* GET /api/movimientos */
    @GetMapping
    public List<MovimientoInventarioResponse> listar() {
        return movimientoService.listar();
    }

    /* GET /api/movimientos/{id} */
    @GetMapping("/{id}")
    public MovimientoInventarioResponse buscarPorId(@PathVariable Integer id) {
        return movimientoService.buscarPorId(id);
    }

    /* GET /api/movimientos/mercaderia/{idMercaderia} -> kardex de un producto */
    @GetMapping("/mercaderia/{idMercaderia}")
    public List<MovimientoInventarioResponse> listarPorMercaderia(@PathVariable Integer idMercaderia) {
        return movimientoService.listarPorMercaderia(idMercaderia);
    }
}