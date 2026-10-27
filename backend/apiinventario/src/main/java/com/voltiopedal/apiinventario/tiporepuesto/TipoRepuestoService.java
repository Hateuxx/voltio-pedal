package com.voltiopedal.apiinventario.tiporepuesto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;

import java.util.List;

@Service 
public class TipoRepuestoService {
    private final TipoRepuestoRepository tipoRepuestoRepository;

    public TipoRepuestoService(TipoRepuestoRepository tipoRepuestoRepository){
        this.tipoRepuestoRepository = tipoRepuestoRepository;
    }

    /*METODO LISTAR */
    public List<TipoRepuestoResponse> listar(){
        return tipoRepuestoRepository.findAllByOrderByNombreAsc().stream().map(this::aResponse).toList();
    }

    /*METODO REGISTRAR */  
    @Transactional 
    public TipoRepuestoResponse registrar(TipoRepuestoRequest request){
        /*Limpiar el nombre: quita los espacios de los extremos y junta los repetidos*/
        String nombre = request.nombre().trim().replaceAll("\\s+"," ");

        /* Revisar si ya existe un tipo con ese nombre */
        if (tipoRepuestoRepository.existsByNombre(nombre)) {
            throw new ReglaNegocioException("ya existe un tipo de respuesto con el nombre: " + nombre);
        }

        /*Crear la entidad, ponerle el nombre y guardarla */
        TipoRepuesto tipo = new TipoRepuesto();
        tipo.setNombre(nombre);
        TipoRepuesto guardado = tipoRepuestoRepository.save(tipo);

        /*devolver el response */
        return aResponse(guardado);
    }

    private TipoRepuestoResponse aResponse(TipoRepuesto t){
        return new TipoRepuestoResponse(
                t.getIdTipoRepuesto(),
                t.getNombre()
        );
    }
}
