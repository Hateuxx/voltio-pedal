package com.voltiopedal.apiinventario.tiporepuesto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;

import java.util.List;

@Service 
public class TipoRepuestoService {
    private final TipoRepuestoRepository tipoRepuestoRepository;

    public TipoRepuestoService(TipoRepuestoRepository tipoRepuestoRepository){
        this.tipoRepuestoRepository = tipoRepuestoRepository;
    }

    /* ==============================
     *          C - CREAR
     * ============================== */

    /*METODO REGISTRAR */  
    @Transactional 
    public TipoRepuestoResponse registrar(TipoRepuestoRequest request){
        /*Limpiar el nombre: quita los espacios de los extremos y junta los repetidos*/
        String nombre = request.nombre().trim().replaceAll("\\s+"," ");

        /* Revisar si ya existe un tipo con ese nombre */
        if (tipoRepuestoRepository.existsByNombre(nombre)) {
            throw new ReglaNegocioException("Ya existe un tipo de repuesto con el nombre: " + nombre);
        }

        /*Crear la entidad, ponerle el nombre y guardarla */
        TipoRepuesto tipo = new TipoRepuesto();
        tipo.setNombre(nombre);
        TipoRepuesto guardado = tipoRepuestoRepository.save(tipo);

        /*devolver el response */
        return aResponse(guardado);
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /*METODO LISTAR */
    public List<TipoRepuestoResponse> listar(){
        return tipoRepuestoRepository.findAllByOrderByNombreAsc().stream().map(this::aResponse).toList();
    }

    /* BUSCAR POR ID */
    public TipoRepuestoResponse buscarPorId(Integer id) {
        return aResponse(obtenerPorId(id));
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    /* ACTUALIZAR (U): cambia el nombre; los repuestos que usan este tipo ven el cambio */
    @Transactional
    public TipoRepuestoResponse actualizar(Integer id, TipoRepuestoRequest request) {
        TipoRepuesto tipo = obtenerPorId(id);

        /* Mismo limpiado del nombre que en registrar */
        String nombre = request.nombre().trim().replaceAll("\\s+", " ");

        if (tipoRepuestoRepository.existsByNombreAndIdTipoRepuestoNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe otro tipo de repuesto con el nombre: " + nombre);
        }

        tipo.setNombre(nombre);
        return aResponse(tipoRepuestoRepository.save(tipo));
    }

    /* ==============================
     *       D - ELIMINAR FÍSICO
     * ============================== */

    /* ELIMINACIÓN FÍSICA (D): borra la fila de la base de datos */
    @Transactional
    public void eliminarFisico(Integer id) {
        TipoRepuesto tipo = obtenerPorId(id);
        tipoRepuestoRepository.delete(tipo);
        /* flush ejecuta el DELETE ahora mismo; si algún repuesto usa este tipo,
         * la base de datos lo rechaza aquí y el usuario recibe el 409 */
        tipoRepuestoRepository.flush();
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* MÉTODO PRIVADO: busca por id o lanza el 404 */
    private TipoRepuesto obtenerPorId(Integer id) {
        return tipoRepuestoRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún tipo de repuesto con el id: " + id));
    }

    /* MÉTODO PRIVADO: convierte Entidad -> Response */
    private TipoRepuestoResponse aResponse(TipoRepuesto t){
        return new TipoRepuestoResponse(
                t.getIdTipoRepuesto(),
                t.getNombre()
        );
    }
}