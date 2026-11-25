package com.voltiopedal.apiinventario.cliente;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /* POST /api/clientes (admin y operador) */
    @PostMapping
    public ResponseEntity<ClienteResponse> registrar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrar(request));
    }

    /* GET /api/clientes */
    @GetMapping
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    /* GET /api/clientes/{id} */
    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable Integer id) {
        return clienteService.buscarPorId(id);
    }

    /* GET /api/clientes/documento/{tipo}/{numero} */
    @GetMapping("/documento/{tipo}/{numero}")
    public ClienteResponse buscarPorDocumento(@PathVariable TipoDocumentoCliente tipo, @PathVariable String numero) {
        return clienteService.buscarPorDocumento(tipo, numero);
    }

    /* PUT /api/clientes/{id} (solo admin) */
    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    /* DELETE /api/clientes/{id} (solo admin): eliminación física, bloqueada si tiene ventas o servicios */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
