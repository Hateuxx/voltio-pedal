package com.voltiopedal.apiinventario.venta;

import com.voltiopedal.apiinventario.cliente.Cliente;
import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaService {
    private final VentaRepository ventaRepository;

    /* Insertar dependencia por constructor */
    public VentaService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* Lista las ventas activas con los datos de su cliente */
    public List<VentaResponse> listarActivas() {
        return ventaRepository.listarActivasConCliente().stream().map(this::aResponse).toList();
    }

    /* Busca una venta por su id */
    public VentaResponse buscarPorId(Integer id) {
        Venta venta = ventaRepository.buscarPorIdConCliente(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ninguna venta con el id: " + id));
        return aResponse(venta);
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* MÉTODO PRIVADO: convierte Entidad -> Response */
    private VentaResponse aResponse(Venta v) {
        /* El cliente puede ser null (venta al paso) */
        Cliente c = v.getCliente();
        return new VentaResponse(
                v.getIdVenta(),
                v.getTipoComprobante(),
                v.getNumeroComprobante(),
                v.getFechaHora(),
                v.getTotal(),
                v.getIdUsuario(),
                v.getActivo(),
                c != null ? c.getIdCliente() : null,
                c != null ? c.getNumeroDocumento() : null,
                c != null ? c.getNombres() : null,
                c != null ? c.getApellidos() : null);
    }
}