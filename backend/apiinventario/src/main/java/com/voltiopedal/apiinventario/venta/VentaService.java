package com.voltiopedal.apiinventario.venta;

import com.voltiopedal.apiinventario.auditoria.AccionAuditoria;
import com.voltiopedal.apiinventario.auditoria.AuditoriaService;
import com.voltiopedal.apiinventario.cliente.Cliente;
import com.voltiopedal.apiinventario.cliente.ClienteRepository;
import com.voltiopedal.apiinventario.cliente.TipoDocumentoCliente;
import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.inventario.KardexService;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import com.voltiopedal.apiinventario.mercaderia.Scooter;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VentaService {
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ClienteRepository clienteRepository;
    private final MercaderiaRepository mercaderiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final KardexService kardexService;
    private final AuditoriaService auditoriaService;

    public VentaService(VentaRepository ventaRepository, DetalleVentaRepository detalleVentaRepository,
                        ClienteRepository clienteRepository, MercaderiaRepository mercaderiaRepository,
                        UsuarioRepository usuarioRepository, KardexService kardexService,
                        AuditoriaService auditoriaService) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.clienteRepository = clienteRepository;
        this.mercaderiaRepository = mercaderiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.kardexService = kardexService;
        this.auditoriaService = auditoriaService;
    }

    /* ==============================
     *          C - REGISTRAR
     * ============================== */

    /* TODO o NADA: si falta stock en una línea, no se guarda la venta ni se descuenta nada */
    @Transactional
    public VentaResponse registrar(VentaRequest request, Integer idUsuario) {
        Cliente cliente = null;
        if (request.idCliente() != null) {
            cliente = clienteRepository.findById(request.idCliente()).orElseThrow(
                    () -> new RecursoNoEncontradoException("No se encontró ningún cliente con el id: " + request.idCliente()));
        }
        if (request.tipoComprobante() == TipoComprobante.FACTURA
                && (cliente == null || cliente.getTipoDocumento() != TipoDocumentoCliente.RUC)) {
            throw new ReglaNegocioException("La factura exige un cliente con RUC. Para otros clientes emita boleta.");
        }

        validarLineas(request.detalles());

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setIdUsuario(idUsuario);
        venta.setTipoComprobante(request.tipoComprobante());
        venta.setNumeroComprobante(siguienteNumero(request.tipoComprobante()));
        venta.setFechaHora(LocalDateTime.now());
        venta.setTotal(BigDecimal.ZERO);
        ventaRepository.save(venta);                 /* IDENTITY: ya tenemos id para el kardex */

        BigDecimal total = BigDecimal.ZERO;
        for (VentaRequest.Linea linea : request.detalles()) {
            Mercaderia mercaderia = mercaderiaRepository.buscarParaActualizarStock(linea.idMercaderia()).orElseThrow(
                    () -> new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + linea.idMercaderia()));
            if (!mercaderia.getActivo()) {
                throw new ReglaNegocioException("La mercadería " + mercaderia.getSku() + " está dada de baja.");
            }

            String serie = vacioANull(linea.numeroSerie());
            if (mercaderia instanceof Scooter) {
                if (serie == null) {
                    throw new ReglaNegocioException("El scooter " + mercaderia.getSku() + " requiere número de serie (garantía).");
                }
                if (linea.cantidad() != 1) {
                    throw new ReglaNegocioException("Cada scooter se vende en una línea propia con cantidad 1 y su número de serie.");
                }
                serie = serie.toUpperCase();
                if (detalleVentaRepository.existsByNumeroSerie(serie)) {
                    throw new ReglaNegocioException("El número de serie " + serie + " ya fue vendido.");
                }
            } else if (serie != null) {
                throw new ReglaNegocioException("Solo los scooters llevan número de serie (" + mercaderia.getSku() + ").");
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setMercaderia(mercaderia);
            detalle.setCantidad(linea.cantidad());
            detalle.setPrecioUnitario(mercaderia.getPrecioVenta());
            detalle.setCostoUnitario(mercaderia.getPrecioCosto());
            detalle.setNumeroSerie(serie);
            venta.agregarDetalle(detalle);

            /* Descuenta stock y deja el SALIDA_VENTA en el kardex (lanza error si no alcanza) */
            kardexService.registrarSalidaPorVenta(mercaderia, linea.cantidad(), idUsuario, venta.getIdVenta());
            total = total.add(mercaderia.getPrecioVenta().multiply(BigDecimal.valueOf(linea.cantidad())));
        }

        venta.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        return aResponse(venta, nombresDe(Set.of(idUsuario)));
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    @Transactional(readOnly = true)
    public List<VentaResponse> listar(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(30);
        List<Venta> ventas = ventaRepository.buscarPorRango(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());
        Map<Integer, String> nombres = nombresDe(ventas.stream().map(v -> v.getIdUsuario()).collect(Collectors.toSet()));
        return ventas.stream().map(v -> aResponse(v, nombres)).toList();
    }

    @Transactional(readOnly = true)
    public VentaResponse buscarPorId(Integer id) {
        Venta venta = obtenerPorId(id);
        return aResponse(venta, nombresDe(Set.of(venta.getIdUsuario())));
    }

    /* ==============================
     *   ANULAR (devuelve el stock)
     * ============================== */

    @Transactional
    public VentaResponse anular(Integer id, String motivo, Integer idUsuario) {
        Venta venta = obtenerPorId(id);
        if (!venta.getActivo()) {
            throw new ReglaNegocioException("La venta " + venta.getNumeroComprobante() + " ya está anulada.");
        }

        String observacion = "Anulación " + venta.getNumeroComprobante() + ": " + motivo.trim();
        for (DetalleVenta detalle : venta.getDetalles()) {
            Mercaderia mercaderia = mercaderiaRepository
                    .buscarParaActualizarStock(detalle.getMercaderia().getIdMercaderia()).orElseThrow();
            kardexService.registrarDevolucionPorVenta(mercaderia, detalle.getCantidad(), idUsuario,
                    venta.getIdVenta(), abreviar(observacion, 255));
        }

        venta.setActivo(false);
        auditoriaService.registrar(idUsuario, AccionAuditoria.INACTIVACION, "VENTA", venta.getNumeroComprobante(), motivo);
        return aResponse(venta, nombresDe(Set.of(venta.getIdUsuario())));
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* Los productos que no son scooter no pueden repetirse (se suman en una sola línea) */
    private void validarLineas(List<VentaRequest.Linea> lineas) {
        Set<Integer> vistos = new HashSet<>();
        Set<String> series = new HashSet<>();
        for (VentaRequest.Linea linea : lineas) {
            String serie = vacioANull(linea.numeroSerie());
            if (serie != null) {
                if (!series.add(serie.toUpperCase())) {
                    throw new ReglaNegocioException("El número de serie " + serie.toUpperCase() + " está repetido en la venta.");
                }
            } else if (!vistos.add(linea.idMercaderia())) {
                throw new ReglaNegocioException("Un producto aparece más de una vez. Sume las cantidades en una sola línea.");
            }
        }
    }

    /* B001-00000001, F001-00000001... */
    private String siguienteNumero(TipoComprobante tipo) {
        String serie = tipo == TipoComprobante.BOLETA ? "B001-" : "F001-";
        int siguiente = ventaRepository.ultimoNumero(tipo)
                .filter(n -> n.startsWith(serie))
                .map(n -> Integer.parseInt(n.substring(serie.length())) + 1)
                .orElse(1);
        return serie + String.format("%08d", siguiente);
    }

    private Venta obtenerPorId(Integer id) {
        return ventaRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ninguna venta con el id: " + id));
    }

    private Map<Integer, String> nombresDe(Set<Integer> ids) {
        return usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(u -> u.getIdUsuario(), u -> u.getNombres() + " " + u.getApellidos()));
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private String abreviar(String texto, int max) {
        return texto.length() > max ? texto.substring(0, max) : texto;
    }

    private VentaResponse aResponse(Venta v, Map<Integer, String> nombres) {
        List<VentaResponse.Linea> lineas = v.getDetalles().stream()
                .map(d -> new VentaResponse.Linea(
                        d.getMercaderia().getIdMercaderia(),
                        d.getMercaderia().getSku(),
                        d.getMercaderia().getDescripcion(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())),
                        d.getNumeroSerie()))
                .toList();

        Cliente c = v.getCliente();
        String cliente = c == null ? null
                : c.getNombres() + (c.getApellidos() != null ? " " + c.getApellidos() : "")
                  + " (" + c.getTipoDocumento() + " " + c.getNumeroDocumento() + ")";

        return new VentaResponse(v.getIdVenta(), v.getTipoComprobante(), v.getNumeroComprobante(), v.getFechaHora(),
                v.getTotal(), v.getActivo(), c == null ? null : c.getIdCliente(), cliente,
                v.getIdUsuario(), nombres.getOrDefault(v.getIdUsuario(), "Usuario #" + v.getIdUsuario()), lineas);
    }
}
