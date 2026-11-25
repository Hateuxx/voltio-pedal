package com.voltiopedal.apiinventario.conteo;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.inventario.KardexService;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConteoService {
    private final ConteoRepository conteoRepository;
    private final MercaderiaRepository mercaderiaRepository;
    private final KardexService kardexService;

    public ConteoService(ConteoRepository conteoRepository, MercaderiaRepository mercaderiaRepository,
                         KardexService kardexService) {
        this.conteoRepository = conteoRepository;
        this.mercaderiaRepository = mercaderiaRepository;
        this.kardexService = kardexService;
    }

    /* 1. ABRIR: solo puede haber un conteo abierto a la vez */
    @Transactional
    public ConteoResponse abrir(ConteoRequests.Abrir request, Integer idUsuario) {
        if (conteoRepository.existsByEstado(EstadoConteo.ABIERTO)) {
            throw new ReglaNegocioException("Ya hay un conteo abierto. Ciérrelo antes de iniciar otro.");
        }
        ConteoInventario conteo = new ConteoInventario();
        conteo.setFechaInicio(LocalDateTime.now());
        conteo.setIdUsuario(idUsuario);
        conteo.setObservacion(vacioANull(request.observacion()));
        return aResponse(conteoRepository.save(conteo));
    }

    @Transactional(readOnly = true)
    public List<ConteoResponse> listar() {
        return conteoRepository.findAllByOrderByFechaInicioDesc().stream().map(c -> aResponse(c)).toList();
    }

    @Transactional(readOnly = true)
    public ConteoResponse buscarPorId(Integer id) {
        return aResponse(obtener(id));
    }

    /* 2. CONTAR: crea o actualiza la línea del producto (upsert) y toma la FOTO del stock actual */
    @Transactional
    public ConteoResponse registrarLinea(Integer idConteo, ConteoRequests.Linea request) {
        ConteoInventario conteo = obtenerAbierto(idConteo);
        Mercaderia mercaderia = mercaderiaRepository.findById(request.idMercaderia()).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + request.idMercaderia()));
        if (!mercaderia.getActivo()) {
            throw new ReglaNegocioException("La mercadería " + mercaderia.getSku() + " está dada de baja.");
        }

        DetalleConteo detalle = conteo.getDetalles().stream()
                .filter(d -> d.getMercaderia().getIdMercaderia().equals(request.idMercaderia()))
                .findFirst()
                .orElse(null);
        if (detalle == null) {
            detalle = new DetalleConteo();
            detalle.setMercaderia(mercaderia);
            conteo.agregarDetalle(detalle);
        }
        detalle.setStockSistema(mercaderia.getStock());
        detalle.setStockFisico(request.stockFisico());
        return aResponse(conteo);
    }

    @Transactional
    public ConteoResponse quitarLinea(Integer idConteo, Integer idMercaderia) {
        ConteoInventario conteo = obtenerAbierto(idConteo);
        boolean quitado = conteo.getDetalles().removeIf(d -> d.getMercaderia().getIdMercaderia().equals(idMercaderia));
        if (!quitado) {
            throw new RecursoNoEncontradoException("Ese producto no está en el conteo.");
        }
        return aResponse(conteo);   /* orphanRemoval: JPA borra la fila del detalle */
    }

    /* 3. CERRAR: aplica las diferencias al stock vía kardex. TODO o NADA */
    @Transactional
    public ConteoResponse cerrar(Integer idConteo, ConteoRequests.Cerrar request, Integer idUsuario) {
        ConteoInventario conteo = obtenerAbierto(idConteo);
        if (conteo.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("No se puede cerrar un conteo sin productos contados.");
        }

        String observacion = "Conteo #" + idConteo + ": " + request.motivo().trim();
        for (DetalleConteo detalle : conteo.getDetalles()) {
            int diferencia = detalle.getDiferencia();
            if (diferencia != 0) {
                Mercaderia mercaderia = mercaderiaRepository
                        .buscarParaActualizarStock(detalle.getMercaderia().getIdMercaderia())
                        .orElseThrow();
                kardexService.registrarAjustePorConteo(mercaderia, diferencia, idUsuario, idConteo, observacion);
                detalle.setAjustado(true);
            }
        }

        conteo.setEstado(EstadoConteo.CERRADO);
        conteo.setFechaCierre(LocalDateTime.now());
        return aResponse(conteo);
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    private ConteoInventario obtener(Integer id) {
        return conteoRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún conteo con el id: " + id));
    }

    private ConteoInventario obtenerAbierto(Integer id) {
        ConteoInventario conteo = obtener(id);
        if (conteo.getEstado() != EstadoConteo.ABIERTO) {
            throw new ReglaNegocioException("El conteo #" + id + " ya está cerrado y no se puede modificar.");
        }
        return conteo;
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private ConteoResponse aResponse(ConteoInventario c) {
        List<ConteoResponse.Linea> lineas = c.getDetalles().stream()
                .map(d -> new ConteoResponse.Linea(
                        d.getMercaderia().getIdMercaderia(),
                        d.getMercaderia().getSku(),
                        d.getMercaderia().getDescripcion(),
                        d.getStockSistema(),
                        d.getStockFisico(),
                        d.getDiferencia(),
                        d.getAjustado()))
                .toList();

        int contados = lineas.size();
        int conDiferencia = (int) lineas.stream().filter(l -> l.diferencia() != 0).count();
        Integer exactitud = contados == 0 ? null : (int) Math.round(100.0 * (contados - conDiferencia) / contados);

        return new ConteoResponse(c.getIdConteo(), c.getFechaInicio(), c.getFechaCierre(), c.getEstado(),
                c.getIdUsuario(), c.getObservacion(), lineas, contados, conDiferencia, exactitud);
    }
}