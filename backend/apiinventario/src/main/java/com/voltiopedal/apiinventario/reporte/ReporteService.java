package com.voltiopedal.apiinventario.reporte;

import com.voltiopedal.apiinventario.inventario.MovimientoInventario;
import com.voltiopedal.apiinventario.inventario.MovimientoInventarioRepository;
import com.voltiopedal.apiinventario.inventario.TipoMovimiento;
import com.voltiopedal.apiinventario.mercaderia.Accesorio;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import com.voltiopedal.apiinventario.mercaderia.Scooter;
import com.voltiopedal.apiinventario.servicio.EstadoServicio;
import com.voltiopedal.apiinventario.servicio.ServicioTecnico;
import com.voltiopedal.apiinventario.servicio.ServicioTecnicoRepository;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import com.voltiopedal.apiinventario.venta.DetalleVenta;
import com.voltiopedal.apiinventario.venta.Venta;
import com.voltiopedal.apiinventario.venta.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/* Solo lectura: todos los cálculos de dinero con BigDecimal */
@Service
@Transactional(readOnly = true)
public class ReporteService {
    private final MercaderiaRepository mercaderiaRepository;
    private final VentaRepository ventaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ServicioTecnicoRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteService(MercaderiaRepository mercaderiaRepository, VentaRepository ventaRepository,
                          MovimientoInventarioRepository movimientoRepository,
                          ServicioTecnicoRepository servicioRepository, UsuarioRepository usuarioRepository) {
        this.mercaderiaRepository = mercaderiaRepository;
        this.ventaRepository = ventaRepository;
        this.movimientoRepository = movimientoRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /* ==============================
     *   INVENTARIO VALORIZADO
     * ============================== */

    public ReporteResponses.Inventario inventario() {
        List<ReporteResponses.LineaInventario> lineas = mercaderiaRepository.findByActivoTrueOrderByDescripcionAsc().stream()
                .map(m -> {
                    BigDecimal stock = BigDecimal.valueOf(m.getStock());
                    return new ReporteResponses.LineaInventario(
                            m.getSku(), m.getDescripcion(), tipoDe(m), m.getProveedor().getRazonSocial(),
                            m.getStock(), m.getStockMinimo(), m.getPrecioCosto(), m.getPrecioVenta(),
                            m.getPrecioCosto().multiply(stock).setScale(2, RoundingMode.HALF_UP),
                            m.getPrecioVenta().multiply(stock).setScale(2, RoundingMode.HALF_UP),
                            m.getStock() <= m.getStockMinimo());
                })
                .toList();

        return new ReporteResponses.Inventario(
                LocalDateTime.now(),
                lineas.size(),
                lineas.stream().mapToInt(l -> l.stock()).sum(),
                (int) lineas.stream().filter(l -> l.bajoMinimo()).count(),
                sumar(lineas.stream().map(l -> l.valorCosto()).toList()),
                sumar(lineas.stream().map(l -> l.valorVenta()).toList()),
                lineas);
    }

    /* ==============================
     *   VENTAS Y UTILIDAD
     * ============================== */

    public ReporteResponses.Ventas ventas(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        List<Venta> todas = ventaRepository.buscarPorRango(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());
        List<Venta> activas = todas.stream().filter(v -> v.getActivo()).toList();

        BigDecimal vendido = BigDecimal.ZERO;
        BigDecimal costo = BigDecimal.ZERO;
        Map<String, Object[]> porProducto = new LinkedHashMap<>();   /* sku -> [descripcion, cantidad, importe, utilidad] */
        for (Venta v : activas) {
            for (DetalleVenta d : v.getDetalles()) {
                BigDecimal cant = BigDecimal.valueOf(d.getCantidad());
                BigDecimal importe = d.getPrecioUnitario().multiply(cant);
                BigDecimal costoLinea = d.getCostoUnitario().multiply(cant);
                vendido = vendido.add(importe);
                costo = costo.add(costoLinea);

                Object[] acc = porProducto.computeIfAbsent(d.getMercaderia().getSku(),
                        k -> new Object[]{d.getMercaderia().getDescripcion(), 0, BigDecimal.ZERO, BigDecimal.ZERO});
                acc[1] = (Integer) acc[1] + d.getCantidad();
                acc[2] = ((BigDecimal) acc[2]).add(importe);
                acc[3] = ((BigDecimal) acc[3]).add(importe.subtract(costoLinea));
            }
        }

        List<ReporteResponses.ProductoVendido> productos = porProducto.entrySet().stream()
                .map(e -> new ReporteResponses.ProductoVendido(e.getKey(), (String) e.getValue()[0], (Integer) e.getValue()[1],
                        dinero((BigDecimal) e.getValue()[2]), dinero((BigDecimal) e.getValue()[3])))
                .sorted(Comparator.comparing((ReporteResponses.ProductoVendido p) -> p.importe()).reversed())
                .toList();

        List<ReporteResponses.VentaDia> porDia = activas.stream()
                .collect(Collectors.groupingBy(v -> v.getFechaHora().toLocalDate(), TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> new ReporteResponses.VentaDia(e.getKey(), e.getValue().size(),
                        sumar(e.getValue().stream().map(v -> v.getTotal()).toList())))
                .toList();

        BigDecimal utilidad = vendido.subtract(costo);
        BigDecimal margen = vendido.signum() == 0 ? BigDecimal.ZERO
                : utilidad.multiply(BigDecimal.valueOf(100)).divide(vendido, 1, RoundingMode.HALF_UP);

        return new ReporteResponses.Ventas(inicio, fin, activas.size(), todas.size() - activas.size(),
                dinero(vendido), dinero(costo), dinero(utilidad), margen, productos, porDia);
    }

    /* ==============================
     *   MOVIMIENTOS DEL ALMACÉN
     * ============================== */

    public List<ReporteResponses.Movimiento> movimientos(LocalDate desde, LocalDate hasta, TipoMovimiento tipo) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(30);
        List<MovimientoInventario> movs = movimientoRepository.buscarPorRango(
                inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay(), tipo);
        Map<Integer, String> nombres = nombresDe(movs.stream().map(m -> m.getIdUsuario()).collect(Collectors.toSet()));
        Set<Integer> idsVenta = movs.stream().map(m -> m.getIdVenta()).filter(id -> id != null).collect(Collectors.toSet());
        Map<Integer, String> comprobantes = ventaRepository.findAllById(idsVenta).stream()
                .collect(Collectors.toMap(v -> v.getIdVenta(), v -> v.getNumeroComprobante()));

        return movs.stream()
                .map(m -> new ReporteResponses.Movimiento(
                        m.getFechaHora(), m.getTipo().name(), m.getTipo().esEntrada(),
                        m.getMercaderia().getSku(), m.getMercaderia().getDescripcion(),
                        m.getCantidad(), m.getStockAnterior(), m.getStockResultante(),
                        nombres.getOrDefault(m.getIdUsuario(), "Usuario #" + m.getIdUsuario()),
                        origenDe(m, comprobantes), m.getObservacion()))
                .toList();
    }

    /* ==============================
     *   SERVICIO TÉCNICO
     * ============================== */

    public ReporteResponses.Servicios servicios(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        List<ServicioTecnico> servicios = servicioRepository.buscarPorRango(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoServicio e : EstadoServicio.values()) {
            porEstado.put(e.name(), servicios.stream().filter(s -> s.getEstado() == e).count());
        }

        Map<Integer, String> nombres = nombresDe(servicios.stream().map(s -> s.getIdTecnico())
                .filter(Objects::nonNull).collect(Collectors.toSet()));
        List<ReporteResponses.ServiciosTecnico> porTecnico = servicios.stream()
                .collect(Collectors.groupingBy(s -> s.getIdTecnico() == null ? -1 : s.getIdTecnico()))
                .entrySet().stream()
                .map(e -> new ReporteResponses.ServiciosTecnico(
                        e.getKey() == -1 ? "Sin asignar" : nombres.getOrDefault(e.getKey(), "Usuario #" + e.getKey()),
                        e.getValue().size(),
                        e.getValue().stream().filter(s -> s.getEstado() == EstadoServicio.ENTREGADO).count()))
                .sorted(Comparator.comparing((ReporteResponses.ServiciosTecnico t) -> t.asignados()).reversed())
                .toList();

        Map<String, Object[]> porRepuesto = new LinkedHashMap<>();
        BigDecimal importe = BigDecimal.ZERO;
        BigDecimal cobrado = BigDecimal.ZERO;
        for (ServicioTecnico s : servicios) {
            for (var d : s.getRepuestos()) {
                BigDecimal sub = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
                importe = importe.add(sub);
                if (!Boolean.TRUE.equals(s.getCubiertoPorGarantia())) {
                    cobrado = cobrado.add(sub);
                }
                Object[] acc = porRepuesto.computeIfAbsent(d.getRepuesto().getSku(),
                        k -> new Object[]{d.getRepuesto().getDescripcion(), 0, BigDecimal.ZERO});
                acc[1] = (Integer) acc[1] + d.getCantidad();
                acc[2] = ((BigDecimal) acc[2]).add(sub);
            }
        }
        List<ReporteResponses.RepuestoConsumido> repuestos = porRepuesto.entrySet().stream()
                .map(e -> new ReporteResponses.RepuestoConsumido(e.getKey(), (String) e.getValue()[0],
                        (Integer) e.getValue()[1], dinero((BigDecimal) e.getValue()[2])))
                .sorted(Comparator.comparing((ReporteResponses.RepuestoConsumido r) -> r.cantidad()).reversed())
                .toList();

        return new ReporteResponses.Servicios(inicio, fin, servicios.size(),
                (int) servicios.stream().filter(s -> s.getEstado() == EstadoServicio.ENTREGADO).count(),
                (int) servicios.stream().filter(s -> Boolean.TRUE.equals(s.getCubiertoPorGarantia())).count(),
                porEstado, porTecnico, repuestos, dinero(importe), dinero(cobrado));
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    private String tipoDe(Mercaderia m) {
        if (m instanceof Scooter) return "SCOOTER";
        if (m instanceof Accesorio) return "ACCESORIO";
        return "REPUESTO";
    }

    private String origenDe(MovimientoInventario m, Map<Integer, String> comprobantes) {
        if (m.getIdIngreso() != null) return "Ingreso #" + m.getIdIngreso();
        if (m.getIdVenta() != null) return "Venta " + comprobantes.getOrDefault(m.getIdVenta(), "#" + m.getIdVenta());
        if (m.getIdServicioTecnico() != null) return "Servicio técnico #" + m.getIdServicioTecnico();
        if (m.getIdConteo() != null) return "Conteo #" + m.getIdConteo();
        return "Ajuste manual";
    }

    private Map<Integer, String> nombresDe(Set<Integer> ids) {
        return usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(u -> u.getIdUsuario(), u -> u.getNombres() + " " + u.getApellidos()));
    }

    private BigDecimal sumar(List<BigDecimal> valores) {
        return dinero(valores.stream().reduce(BigDecimal.ZERO, (a, b) -> a.add(b)));
    }

    private BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
