package com.voltiopedal.apiinventario.ingresomercaderia;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.inventario.KardexService;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import com.voltiopedal.apiinventario.proveedor.Proveedor;
import com.voltiopedal.apiinventario.proveedor.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IngresoMercaderiaService {
    private final IngresoMercaderiaRepository ingresoMercaderiaRepository;
    private final ProveedorRepository proveedorRepository;
    private final MercaderiaRepository mercaderiaRepository;
    private final KardexService kardexService;

    public IngresoMercaderiaService(IngresoMercaderiaRepository ingresoMercaderiaRepository,
                                    ProveedorRepository proveedorRepository,
                                    MercaderiaRepository mercaderiaRepository,
                                    KardexService kardexService) {
        this.ingresoMercaderiaRepository = ingresoMercaderiaRepository;
        this.proveedorRepository = proveedorRepository;
        this.mercaderiaRepository = mercaderiaRepository;
        this.kardexService = kardexService;
    }

    /* ==============================
     *          C - REGISTRAR
     * ============================== */

    /* TODO o NADA: si una línea falla, se revierte la cabecera, los detalles, el stock y el kardex */
    @Transactional
    public IngresoMercaderiaResponse registrar(IngresoRequest request, Integer idUsuario) {
        Proveedor proveedor = proveedorRepository.findById(request.idProveedor()).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún proveedor con el id: " + request.idProveedor()));
        if (!proveedor.getActivo()) {
            throw new ReglaNegocioException("El proveedor " + proveedor.getRazonSocial() + " está dado de baja.");
        }

        String guia = request.numeroGuia().trim().toUpperCase();
        if (ingresoMercaderiaRepository.existsByProveedor_IdProveedorAndNumeroGuia(proveedor.getIdProveedor(), guia)) {
            throw new ReglaNegocioException("La guía " + guia + " de este proveedor ya fue registrada.");
        }

        Set<Integer> vistos = new HashSet<>();
        for (IngresoRequest.Linea linea : request.detalles()) {
            if (!vistos.add(linea.idMercaderia())) {
                throw new ReglaNegocioException("Un producto aparece más de una vez. Sume las cantidades en una sola línea.");
            }
        }

        IngresoMercaderia ingreso = new IngresoMercaderia();
        ingreso.setProveedor(proveedor);
        ingreso.setNumeroGuia(guia);
        ingreso.setFechaHora(LocalDateTime.now());
        ingreso.setIdUsuario(idUsuario);
        ingreso.setObservacion(vacioANull(request.observacion()));
        ingresoMercaderiaRepository.save(ingreso);      /* IDENTITY: el INSERT es inmediato, ya tenemos el id */

        for (IngresoRequest.Linea linea : request.detalles()) {
            Mercaderia mercaderia = mercaderiaRepository.buscarParaActualizarStock(linea.idMercaderia()).orElseThrow(
                    () -> new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + linea.idMercaderia()));

            if (!mercaderia.getActivo()) {
                throw new ReglaNegocioException("La mercadería " + mercaderia.getSku() + " está dada de baja.");
            }
            if (!mercaderia.getProveedor().getIdProveedor().equals(proveedor.getIdProveedor())) {
                throw new ReglaNegocioException("La mercadería " + mercaderia.getSku() + " no pertenece al proveedor "
                        + proveedor.getRazonSocial() + ".");
            }

            DetalleIngreso detalle = new DetalleIngreso();
            detalle.setMercaderia(mercaderia);
            detalle.setCantidad(linea.cantidad());
            detalle.setCostoUnitario(linea.costoUnitario());
            ingreso.agregarDetalle(detalle);

            kardexService.registrarEntradaPorIngreso(mercaderia, linea.cantidad(), idUsuario, ingreso.getIdIngreso());
        }

        return aResponse(ingreso);
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    @Transactional(readOnly = true)
    public List<IngresoMercaderiaResponse> listarActivos() {
        return ingresoMercaderiaRepository.listarActivosConProveedor().stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public IngresoMercaderiaResponse buscarPorId(Integer id) {
        IngresoMercaderia ingreso = ingresoMercaderiaRepository.buscarPorIdConProveedor(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún ingreso con el id: " + id));
        return aResponse(ingreso);
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private IngresoMercaderiaResponse aResponse(IngresoMercaderia i) {
        List<IngresoMercaderiaResponse.Linea> lineas = i.getDetalles().stream()
                .map(d -> new IngresoMercaderiaResponse.Linea(
                        d.getMercaderia().getIdMercaderia(),
                        d.getMercaderia().getSku(),
                        d.getMercaderia().getDescripcion(),
                        d.getCantidad(),
                        d.getCostoUnitario(),
                        d.getCostoUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))))
                .toList();

         BigDecimal total = lineas.stream()
                .map(linea -> linea.subtotal())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        return new IngresoMercaderiaResponse(
                i.getIdIngreso(), i.getNumeroGuia(), i.getFechaHora(), i.getIdUsuario(),
                i.getObservacion(), i.getActivo(),
                i.getProveedor().getIdProveedor(), i.getProveedor().getRuc(), i.getProveedor().getRazonSocial(),
                lineas, total);
    }
}