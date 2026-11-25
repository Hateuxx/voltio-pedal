package com.voltiopedal.apiinventario.servicio;

import com.voltiopedal.apiinventario.cliente.Cliente;
import com.voltiopedal.apiinventario.cliente.ClienteRepository;
import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.inventario.KardexService;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import com.voltiopedal.apiinventario.mercaderia.Repuesto;
import com.voltiopedal.apiinventario.mercaderia.Scooter;
import com.voltiopedal.apiinventario.usuario.Rol;
import com.voltiopedal.apiinventario.usuario.Usuario;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import com.voltiopedal.apiinventario.venta.DetalleVenta;
import com.voltiopedal.apiinventario.venta.DetalleVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioTecnicoService {
    private final ServicioTecnicoRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final MercaderiaRepository mercaderiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final KardexService kardexService;

    public ServicioTecnicoService(ServicioTecnicoRepository servicioRepository, ClienteRepository clienteRepository,
                                  MercaderiaRepository mercaderiaRepository, UsuarioRepository usuarioRepository,
                                  DetalleVentaRepository detalleVentaRepository, KardexService kardexService) {
        this.servicioRepository = servicioRepository;
        this.clienteRepository = clienteRepository;
        this.mercaderiaRepository = mercaderiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.kardexService = kardexService;
    }

    /* ==============================
     *   RF10 - RECEPCIÓN EN TALLER
     * ============================== */

    @Transactional
    public ServicioResponse registrar(ServicioRequests.Registrar request) {
        Cliente cliente = clienteRepository.findById(request.idCliente()).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún cliente con el id: " + request.idCliente()));

        Mercaderia modelo = mercaderiaRepository.findById(request.idScooter()).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + request.idScooter()));
        if (!(modelo instanceof Scooter scooter)) {
            throw new ReglaNegocioException("Solo se reciben scooters en el servicio técnico (" + modelo.getSku() + " no es un scooter).");
        }

        String serie = request.numeroSerie().trim().toUpperCase();
        if (servicioRepository.existsByNumeroSerieAndEstadoNot(serie, EstadoServicio.ENTREGADO)) {
            throw new ReglaNegocioException("El scooter con serie " + serie + " ya tiene una orden de servicio abierta.");
        }

        /* Garantía: si el scooter se vendió en la tienda, la fecha y los meses salen de la venta */
        LocalDate fechaCompra = request.fechaCompra();
        Integer meses = request.mesesGarantia();
        Optional<DetalleVenta> venta = detalleVentaRepository.buscarVentaActivaPorSerie(serie);
        if (venta.isPresent()) {
            if (!venta.get().getMercaderia().getIdMercaderia().equals(scooter.getIdMercaderia())) {
                throw new ReglaNegocioException("La serie " + serie + " fue vendida como otro modelo ("
                        + venta.get().getMercaderia().getSku() + ").");
            }
            fechaCompra = venta.get().getVenta().getFechaHora().toLocalDate();
            meses = scooter.getMesesGarantia();
        } else if (fechaCompra != null && meses == null) {
            meses = scooter.getMesesGarantia();
        }

        LocalDateTime ahora = LocalDateTime.now();
        ServicioTecnico servicio = new ServicioTecnico();
        servicio.setFechaIngreso(ahora);
        servicio.setNumeroSerie(serie);
        servicio.setFallaReportada(request.fallaReportada().trim());
        servicio.setCliente(cliente);
        servicio.setScooter(scooter);
        servicio.setFechaCompra(fechaCompra);
        servicio.setMesesGarantia(fechaCompra == null ? null : meses);
        servicio.setCubiertoPorGarantia(fechaCompra != null && meses != null
                && !ahora.toLocalDate().isAfter(fechaCompra.plusMonths(meses)));
        servicio.setIdTecnico(validarTecnico(request.idTecnico()));
        servicio.setEstado(EstadoServicio.INGRESADO);

        return aResponse(servicioRepository.save(servicio));
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar(EstadoServicio estado) {
        return servicioRepository.buscar(estado).stream().map(s -> aResponse(s)).toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse buscarPorId(Integer id) {
        return aResponse(obtenerPorId(id));
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse.Tecnico> listarTecnicos() {
        return usuarioRepository.findByRolAndActivoTrueOrderByNombresAsc(Rol.TECNICO).stream()
                .map(u -> new ServicioResponse.Tecnico(u.getIdUsuario(), u.getNombres() + " " + u.getApellidos()))
                .toList();
    }

    /* ==============================
     *   AVANCE DEL TÉCNICO (estado)
     * ============================== */

    @Transactional
    public ServicioResponse actualizar(Integer id, ServicioRequests.Actualizar request) {
        ServicioTecnico servicio = obtenerAbierto(id);

        String diagnostico = vacioANull(request.diagnostico());
        if (diagnostico != null) {
            servicio.setDiagnostico(diagnostico);
        }
        if (request.idTecnico() != null) {
            servicio.setIdTecnico(validarTecnico(request.idTecnico()));
        }

        EstadoServicio nuevo = request.estado();
        if (nuevo != EstadoServicio.INGRESADO && servicio.getIdTecnico() == null) {
            throw new ReglaNegocioException("Asigne un técnico antes de avanzar el servicio.");
        }
        if ((nuevo == EstadoServicio.LISTO_PARA_ENTREGA || nuevo == EstadoServicio.ENTREGADO)
                && servicio.getDiagnostico() == null) {
            throw new ReglaNegocioException("Registre el diagnóstico antes de marcar el scooter como listo o entregado.");
        }

        servicio.setEstado(nuevo);
        if (nuevo == EstadoServicio.ENTREGADO) {
            servicio.setFechaEntrega(LocalDateTime.now());    /* CHECK: ENTREGADO <=> fecha_entrega no nula */
        }
        return aResponse(servicio);
    }

    /* ==============================
     *   RF17 - CONSUMO DE REPUESTOS
     * ============================== */

    @Transactional
    public ServicioResponse consumirRepuesto(Integer id, ServicioRequests.Consumo request, Integer idUsuario) {
        ServicioTecnico servicio = obtenerAbierto(id);

        Mercaderia mercaderia = mercaderiaRepository.buscarParaActualizarStock(request.idRepuesto()).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + request.idRepuesto()));
        if (!(mercaderia instanceof Repuesto repuesto)) {
            throw new ReglaNegocioException(mercaderia.getSku() + " no es un repuesto.");
        }
        if (!repuesto.getActivo()) {
            throw new ReglaNegocioException("El repuesto " + repuesto.getSku() + " está dado de baja.");
        }

        DetalleServicio detalle = new DetalleServicio();
        detalle.setRepuesto(repuesto);
        detalle.setCantidad(request.cantidad());
        detalle.setPrecioUnitario(repuesto.getPrecioVenta());
        detalle.setCostoUnitario(repuesto.getPrecioCosto());
        detalle.setFechaHora(LocalDateTime.now());
        servicio.agregarRepuesto(detalle);

        /* Descuenta el stock y deja el SALIDA_REPARACION en el kardex */
        kardexService.registrarSalidaPorReparacion(repuesto, request.cantidad(), idUsuario, servicio.getIdServicioTecnico());
        if (servicio.getEstado() == EstadoServicio.ESPERANDO_REPUESTO) {
            servicio.setEstado(EstadoServicio.EN_REPARACION);
        }
        return aResponse(servicio);
    }

    /* Devuelve al almacén un repuesto que al final no se usó */
    @Transactional
    public ServicioResponse devolverRepuesto(Integer id, Integer idDetalle, Integer idUsuario) {
        ServicioTecnico servicio = obtenerAbierto(id);
        DetalleServicio detalle = servicio.getRepuestos().stream()
                .filter(d -> d.getIdDetalleServicio().equals(idDetalle))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Ese repuesto no está registrado en el servicio."));

        Mercaderia repuesto = mercaderiaRepository.buscarParaActualizarStock(detalle.getRepuesto().getIdMercaderia()).orElseThrow();
        kardexService.registrarDevolucionPorReparacion(repuesto, detalle.getCantidad(), idUsuario, servicio.getIdServicioTecnico());
        servicio.getRepuestos().remove(detalle);          /* orphanRemoval borra la fila */
        return aResponse(servicio);
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    private ServicioTecnico obtenerPorId(Integer id) {
        return servicioRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún servicio técnico con el id: " + id));
    }

    private ServicioTecnico obtenerAbierto(Integer id) {
        ServicioTecnico servicio = obtenerPorId(id);
        if (servicio.getEstado() == EstadoServicio.ENTREGADO) {
            throw new ReglaNegocioException("El servicio #" + id + " ya fue entregado y no se puede modificar.");
        }
        return servicio;
    }

    private Integer validarTecnico(Integer idTecnico) {
        if (idTecnico == null) {
            return null;
        }
        Usuario tecnico = usuarioRepository.findById(idTecnico).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún usuario con el id: " + idTecnico));
        if (tecnico.getRol() != Rol.TECNICO || !Boolean.TRUE.equals(tecnico.getActivo())) {
            throw new ReglaNegocioException("El usuario asignado debe ser un técnico activo.");
        }
        return idTecnico;
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private ServicioResponse aResponse(ServicioTecnico s) {
        List<ServicioResponse.Repuesto> repuestos = s.getRepuestos().stream()
                .map(d -> new ServicioResponse.Repuesto(
                        d.getIdDetalleServicio(),
                        d.getRepuesto().getIdMercaderia(),
                        d.getRepuesto().getSku(),
                        d.getRepuesto().getDescripcion(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())),
                        d.getFechaHora()))
                .toList();
        BigDecimal total = repuestos.stream().map(r -> r.subtotal()).reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        String tecnico = null;
        if (s.getIdTecnico() != null) {
            Map<Integer, String> nombres = usuarioRepository.findAllById(List.of(s.getIdTecnico())).stream()
                    .collect(Collectors.toMap(u -> u.getIdUsuario(), u -> u.getNombres() + " " + u.getApellidos()));
            tecnico = nombres.get(s.getIdTecnico());
        }

        Cliente c = s.getCliente();
        String cliente = c.getNombres() + (c.getApellidos() != null ? " " + c.getApellidos() : "")
                + " (" + c.getTipoDocumento() + " " + c.getNumeroDocumento() + ")";
        LocalDate hasta = (s.getFechaCompra() != null && s.getMesesGarantia() != null)
                ? s.getFechaCompra().plusMonths(s.getMesesGarantia()) : null;

        return new ServicioResponse(
                s.getIdServicioTecnico(), s.getFechaIngreso(), s.getFechaEntrega(), s.getNumeroSerie(),
                s.getFallaReportada(), s.getDiagnostico(), s.getEstado(), s.getFechaCompra(), s.getMesesGarantia(),
                hasta, s.getCubiertoPorGarantia(), c.getIdCliente(), cliente,
                s.getScooter().getIdMercaderia(), s.getScooter().getSku() + " · " + s.getScooter().getDescripcion(),
                s.getIdTecnico(), tecnico, repuestos, total,
                Objects.equals(s.getCubiertoPorGarantia(), true) ? BigDecimal.ZERO : total);
    }
}
