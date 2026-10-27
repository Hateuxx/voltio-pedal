package com.voltiopedal.apiinventario.tiporepuesto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/tipos-repuesto")
public class TipoRepuestoController {
    private final TipoRepuestoService tipoRepuestoService;

    /*constructor */
    public TipoRepuestoController(TipoRepuestoService tipoRepuestoService){
        this.tipoRepuestoService=tipoRepuestoService;
    }

    @GetMapping 
    public List<TipoRepuestoResponse> listar(){
        return tipoRepuestoService.listar();
    }

    @PostMapping 
    public ResponseEntity<TipoRepuestoResponse> registrar(@Valid @RequestBody TipoRepuestoRequest request){
        TipoRepuestoResponse creado = tipoRepuestoService.registrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
    
}
