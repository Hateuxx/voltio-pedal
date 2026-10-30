package com.voltiopedal.apiinventario.ingresomercaderia;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngresoMercaderiaService {
    private final IngresoMercaderiaRepository ingresoMercaderiaRepository;

    /* Insertar dependencia por constructor */
    public IngresoMercaderiaService(IngresoMercaderiaRepository ingresoMercaderiaRepository) {
        this.ingresoMercaderiaRepository = ingresoMercaderiaRepository;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* Lista los ingresos activos con los datos de su proveedor */
    public List<IngresoMercaderiaResponse> listarActivos() {
        return ingresoMercaderiaRepository.listarActivosConProveedor().stream().map(this::aResponse).toList();
    }

    /* Busca un ingreso por su id */
    public IngresoMercaderiaResponse buscarPorId(Integer id) {
        IngresoMercaderia ingreso = ingresoMercaderiaRepository.buscarPorIdConProveedor(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún ingreso con el id: " + id));
        return aResponse(ingreso);
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* MÉTODO PRIVADO: convierte Entidad -> Response */
    private IngresoMercaderiaResponse aResponse(IngresoMercaderia i) {
        return new IngresoMercaderiaResponse(
                i.getIdIngreso(),
                i.getNumeroGuia(),
                i.getFechaHora(),
                i.getIdUsuario(),
                i.getObservacion(),
                i.getActivo(),
                i.getProveedor().getIdProveedor(),
                i.getProveedor().getRuc(),
                i.getProveedor().getRazonSocial());
    }
}