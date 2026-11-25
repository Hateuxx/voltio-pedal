package com.voltiopedal.apiinventario.reporte;

import com.voltiopedal.apiinventario.cliente.Cliente;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.movimiento.MovimientoInventario;
import com.voltiopedal.apiinventario.movimiento.MovimientoInventarioRepository;
import com.voltiopedal.apiinventario.serviciotecnico.EstadoServicio;
import com.voltiopedal.apiinventario.serviciotecnico.ServicioTecnico;
import com.voltiopedal.apiinventario.serviciotecnico.ServicioTecnicoRepository;
import com.voltiopedal.apiinventario.usuario.Usuario;
import com.voltiopedal.apiinventario.venta.TipoComprobante;
import com.voltiopedal.apiinventario.venta.Venta;
import org.springframework.stereotype.Service;
import com.voltiopedal.apiinventario.movimiento.TipoMovimiento;
import com.voltiopedal.apiinventario.venta.DetalleVenta;
import com.voltiopedal.apiinventario.venta.DetalleVentaRepository;
import com.voltiopedal.apiinventario.mercaderia.Accesorio;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.Repuesto;
import com.voltiopedal.apiinventario.mercaderia.Scooter;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import com.voltiopedal.apiinventario.auditoria.AccionAuditoria;
import com.voltiopedal.apiinventario.auditoria.AuditoriaService;
import com.voltiopedal.apiinventario.auditoria.MotivoBaja;
import com.voltiopedal.apiinventario.auditoria.RegistroAuditoria;
import com.voltiopedal.apiinventario.auditoria.RegistroAuditoriaRepository;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {
    private final DetalleVentaRepository detalleVentaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ServicioTecnicoRepository servicioRepository;
    private final MercaderiaRepository mercaderiaRepository;
    private final RegistroAuditoriaRepository auditoriaRepository;

    /* Insertar dependencias por constructor */
    public ReporteService(DetalleVentaRepository detalleVentaRepository,
            MovimientoInventarioRepository movimientoRepository,
            ServicioTecnicoRepository servicioRepository,
            MercaderiaRepository mercaderiaRepository,
            RegistroAuditoriaRepository auditoriaRepository) {
        this.detalleVentaRepository = detalleVentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.servicioRepository = servicioRepository;
        this.mercaderiaRepository = mercaderiaRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    /*
     * ==============================
     * REPORTE 1: VENTAS (una fila por producto vendido)
     * ==============================
     */

    public ReporteResponse<FilasReporte.VentaDetalleFila> ventas(LocalDate fechaInicio, LocalDate fechaFin) {
        validarRango(fechaInicio, fechaFin);

        List<DetalleVenta> lineas = detalleVentaRepository.listarPorRangoConVentaYProducto(inicioDelDia(fechaInicio),
                finDelDia(fechaFin));

        /* Cada línea de venta se convierte en una fila del reporte */
        List<FilasReporte.VentaDetalleFila> filas = lineas.stream()
                .map(d -> new FilasReporte.VentaDetalleFila(
                        d.getVenta().getFechaHora(),
                        d.getVenta().getTipoComprobante(),
                        d.getVenta().getNumeroComprobante(),
                        nombreCliente(d.getVenta().getCliente()),
                        d.getMercaderia().getSku(),
                        d.getMercaderia().getDescripcion(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())),
                        d.getVenta().getTotal()))
                .toList();

        /* Las ventas distintas (una venta tiene varias líneas), sin repetir */
        Map<Integer, Venta> ventas = new LinkedHashMap<>();
        for (DetalleVenta d : lineas) {
            ventas.putIfAbsent(d.getVenta().getIdVenta(), d.getVenta());
        }

        /*
         * Monto total: suma de los totales de cada venta (el dinero sí se puede sumar)
         */
        BigDecimal montoTotal = ventas.values().stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        /*
         * Venta de mayor y de menor monto, y ticket promedio (por venta, no por línea)
         */
        Destacado ventaMayor = ventas.values().stream()
                .max(Comparator.comparing(Venta::getTotal))
                .map(v -> new Destacado(nombreVenta(v), v.getTotal()))
                .orElse(null);
        Destacado ventaMenor = ventas.values().stream()
                .min(Comparator.comparing(Venta::getTotal))
                .map(v -> new Destacado(nombreVenta(v), v.getTotal()))
                .orElse(null);
        BigDecimal ticketPromedio = ventas.isEmpty() ? null
                : montoTotal.divide(BigDecimal.valueOf(ventas.size()), 2, RoundingMode.HALF_UP);

        /*
         * Unidades vendidas por producto: se suman solo las unidades del MISMO producto
         */
        Map<String, Integer> unidadesPorSku = new LinkedHashMap<>();
        Map<String, String> nombrePorSku = new HashMap<>();
        for (DetalleVenta d : lineas) {
            String sku = d.getMercaderia().getSku();
            unidadesPorSku.merge(sku, d.getCantidad(), Integer::sum);
            nombrePorSku.putIfAbsent(sku, sku + " - " + d.getMercaderia().getDescripcion());
        }
        /* Si hay empate, gana el producto de menor código */
        Comparator<Map.Entry<String, Integer>> masUnidades = (a, b) -> {
            int c = Integer.compare(b.getValue(), a.getValue());
            return c != 0 ? c : a.getKey().compareTo(b.getKey());
        };
        Comparator<Map.Entry<String, Integer>> menosUnidades = (a, b) -> {
            int c = Integer.compare(a.getValue(), b.getValue());
            return c != 0 ? c : a.getKey().compareTo(b.getKey());
        };
        Destacado productoMasVendido = unidadesPorSku.entrySet().stream()
                .min(masUnidades)
                .map(e -> new Destacado(nombrePorSku.get(e.getKey()), BigDecimal.valueOf(e.getValue())))
                .orElse(null);
        Destacado productoMenosVendido = unidadesPorSku.entrySet().stream()
                .min(menosUnidades)
                .map(e -> new Destacado(nombrePorSku.get(e.getKey()), BigDecimal.valueOf(e.getValue())))
                .orElse(null);

        /*
         * Monto vendido por categoría (en soles, para no mezclar unidades de productos
         * distintos)
         */
        Map<String, BigDecimal> montoPorCategoria = new LinkedHashMap<>();
        for (DetalleVenta d : lineas) {
            montoPorCategoria.merge(categoriaDe(d.getMercaderia()),
                    d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())), BigDecimal::add);
        }
        Destacado categoriaMasVendida = montoPorCategoria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> new Destacado(e.getKey(), e.getValue()))
                .orElse(null);
        Destacado categoriaMenosVendida = montoPorCategoria.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(e -> new Destacado(e.getKey(), e.getValue()))
                .orElse(null);

        /* Resumen: totales y estadísticas (los datos que no existen quedan en null) */
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalVentas", ventas.size());
        resumen.put("montoTotal", montoTotal);
        resumen.put("cantidadBoletas",
                ventas.values().stream().filter(v -> v.getTipoComprobante() == TipoComprobante.BOLETA).count());
        resumen.put("cantidadFacturas",
                ventas.values().stream().filter(v -> v.getTipoComprobante() == TipoComprobante.FACTURA).count());
        resumen.put("ticketPromedio", ticketPromedio);
        resumen.put("ventaMayor", ventaMayor);
        resumen.put("ventaMenor", ventaMenor);
        resumen.put("productoMasVendido", productoMasVendido);
        resumen.put("productoMenosVendido", productoMenosVendido);
        resumen.put("categoriaMasVendida", categoriaMasVendida);
        resumen.put("categoriaMenosVendida", categoriaMenosVendida);

        return new ReporteResponse<>("Ventas realizadas", fechaInicio, fechaFin, filas.size(), resumen, filas);
    }

    /*
     * ==============================
     * REPORTES 2 Y 3: ENTRADAS Y SALIDAS DE INVENTARIO
     * ==============================
     */

    /*
     * Movimientos que SUMAN stock: compras a proveedores, devoluciones y ajustes
     * hacia arriba
     */
    public ReporteResponse<FilasReporte.MovimientoFila> entradas(LocalDate fechaInicio, LocalDate fechaFin) {
        return reporteMovimientos("Entradas de almacenamiento", true, fechaInicio, fechaFin);
    }

    /*
     * Movimientos que RESTAN stock: ventas, repuestos usados, devoluciones a
     * proveedores y ajustes hacia abajo
     */
    public ReporteResponse<FilasReporte.MovimientoFila> salidas(LocalDate fechaInicio, LocalDate fechaFin) {
        return reporteMovimientos("Salidas de inventario", false, fechaInicio, fechaFin);
    }

    /*
     * Parte común de los dos reportes: se queda solo con los movimientos que suman
     * (o restan) stock
     */
    private ReporteResponse<FilasReporte.MovimientoFila> reporteMovimientos(String titulo, boolean entradas,
            LocalDate fechaInicio, LocalDate fechaFin) {
        validarRango(fechaInicio, fechaFin);

        List<MovimientoInventario> movimientos = movimientoRepository
                .listarPorRangoConMercaderia(inicioDelDia(fechaInicio), finDelDia(fechaFin))
                .stream()
                .filter(m -> m.getTipo().aumentaStock() == entradas)
                .toList();

        List<FilasReporte.MovimientoFila> filas = movimientos.stream()
                .map(m -> new FilasReporte.MovimientoFila(
                        m.getFechaHora(),
                        m.getTipo(),
                        m.getTipo().aumentaStock(),
                        m.getMercaderia().getSku(),
                        m.getMercaderia().getDescripcion(),
                        m.getCantidad(),
                        m.getStockAnterior(),
                        m.getStockResultante(),
                        m.getObservacion()))
                .toList();

        /*
         * Cuántos movimientos hay de cada tipo (se cuentan movimientos, no unidades de
         * productos distintos)
         */
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoMovimiento tipo : TipoMovimiento.values()) {
            if (tipo.aumentaStock() == entradas) {
                porTipo.put(tipo.name(), movimientos.stream().filter(m -> m.getTipo() == tipo).count());
            }
        }

        /* Unidades por producto: se suman solo las unidades del MISMO producto */
        Map<String, Integer> unidadesPorSku = new LinkedHashMap<>();
        Map<String, String> nombrePorSku = new HashMap<>();
        for (MovimientoInventario m : movimientos) {
            String sku = m.getMercaderia().getSku();
            unidadesPorSku.merge(sku, m.getCantidad(), Integer::sum);
            nombrePorSku.putIfAbsent(sku, sku + " - " + m.getMercaderia().getDescripcion());
        }

        /* Movimiento de mayor y de menor cantidad (un solo registro) */
        Destacado movimientoMayor = movimientos.stream()
                .max(Comparator.comparing(MovimientoInventario::getCantidad))
                .map(m -> new Destacado(m.getMercaderia().getSku() + " - " + m.getMercaderia().getDescripcion(),
                        BigDecimal.valueOf(m.getCantidad())))
                .orElse(null);
        Destacado movimientoMenor = movimientos.stream()
                .min(Comparator.comparing(MovimientoInventario::getCantidad))
                .map(m -> new Destacado(m.getMercaderia().getSku() + " - " + m.getMercaderia().getDescripcion(),
                        BigDecimal.valueOf(m.getCantidad())))
                .orElse(null);

        /* Promedio de unidades por movimiento (un decimal) */
        BigDecimal promedio = movimientos.isEmpty() ? null
                : BigDecimal.valueOf(movimientos.stream().mapToInt(MovimientoInventario::getCantidad).sum())
                        .divide(BigDecimal.valueOf(movimientos.size()), 1, RoundingMode.HALF_UP);

        /*
         * Productos activos que no tuvieron ningún movimiento de este tipo en el rango
         */
        List<Mercaderia> activos = mercaderiaRepository.findByActivoTrue();
        long sinMovimiento = activos.stream().filter(p -> !unidadesPorSku.containsKey(p.getSku())).count();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalMovimientos", movimientos.size());
        resumen.put("porTipo", porTipo);
        resumen.put("productosConMovimiento", unidadesPorSku.size());
        resumen.put("productoMayor", productoExtremo(unidadesPorSku, nombrePorSku, true));
        resumen.put("productoMenor", productoExtremo(unidadesPorSku, nombrePorSku, false));
        resumen.put("movimientoMayor", movimientoMayor);
        resumen.put("movimientoMenor", movimientoMenor);
        resumen.put("promedioPorMovimiento", promedio);
        resumen.put("productosSinMovimiento", (int) sinMovimiento);
        resumen.put("totalProductosActivos", activos.size());

        return new ReporteResponse<>(titulo, fechaInicio, fechaFin, filas.size(), resumen, filas);
    }

    /*
     * ==============================
     * REPORTE 4: ATENCIONES DE SERVICIO TÉCNICO
     * ==============================
     */

    public ReporteResponse<FilasReporte.ServicioFila> servicioTecnico(LocalDate fechaInicio, LocalDate fechaFin) {
        validarRango(fechaInicio, fechaFin);

        List<ServicioTecnico> servicios = servicioRepository.listarPorRangoConRelaciones(inicioDelDia(fechaInicio),
                finDelDia(fechaFin));

        List<FilasReporte.ServicioFila> filas = servicios.stream()
                .map(s -> new FilasReporte.ServicioFila(
                        s.getFechaIngreso(),
                        s.getFechaEntrega(),
                        nombreCliente(s.getCliente()),
                        s.getScooter().getSku() + " - " + s.getScooter().getDescripcion(),
                        s.getNumeroSerie(),
                        s.getFallaReportada(),
                        s.getEstado(),
                        nombreTecnico(s.getTecnico()),
                        s.getCubiertoPorGarantia()))
                .toList();

        /* Cuántos servicios hay en cada estado y cuántos estuvieron en garantía */
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoServicio estado : EstadoServicio.values()) {
            porEstado.put(estado.name(), servicios.stream().filter(s -> s.getEstado() == estado).count());
        }

        /*
         * Servicios entregados: tiempo de atención = fecha de entrega menos fecha de
         * ingreso (en minutos)
         */
        List<ServicioTecnico> entregados = servicios.stream()
                .filter(s -> s.getEstado() == EstadoServicio.ENTREGADO && s.getFechaEntrega() != null)
                .toList();
        Comparator<ServicioTecnico> porTiempoAtencion = Comparator.comparingLong(this::minutosDeAtencion);
        Destacado tiempoMayor = entregados.stream().max(porTiempoAtencion)
                .map(s -> new Destacado(s.getNumeroSerie(), BigDecimal.valueOf(minutosDeAtencion(s))))
                .orElse(null);
        Destacado tiempoMenor = entregados.stream().min(porTiempoAtencion)
                .map(s -> new Destacado(s.getNumeroSerie(), BigDecimal.valueOf(minutosDeAtencion(s))))
                .orElse(null);
        BigDecimal tiempoPromedio = entregados.isEmpty() ? null
                : BigDecimal.valueOf(entregados.stream().mapToLong(this::minutosDeAtencion).sum())
                        .divide(BigDecimal.valueOf(entregados.size()), 0, RoundingMode.HALF_UP);

        /*
         * Servicios pendientes: antigüedad = tiempo desde el ingreso hasta este momento
         * (en minutos)
         */
        LocalDateTime ahora = LocalDateTime.now();
        List<ServicioTecnico> pendientes = servicios.stream()
                .filter(s -> s.getEstado() != EstadoServicio.ENTREGADO)
                .toList();
        Comparator<ServicioTecnico> porAntiguedad = Comparator
                .comparingLong((ServicioTecnico s) -> Duration.between(s.getFechaIngreso(), ahora).toMinutes());
        Destacado antiguedadMayor = pendientes.stream().max(porAntiguedad)
                .map(s -> new Destacado(s.getNumeroSerie(),
                        BigDecimal.valueOf(Duration.between(s.getFechaIngreso(), ahora).toMinutes())))
                .orElse(null);
        BigDecimal antiguedadPromedio = pendientes.isEmpty() ? null
                : BigDecimal.valueOf(pendientes.stream()
                        .mapToLong(s -> Duration.between(s.getFechaIngreso(), ahora).toMinutes()).sum())
                        .divide(BigDecimal.valueOf(pendientes.size()), 0, RoundingMode.HALF_UP);

        /*
         * Resumen: totales y estadísticas (los tiempos van en minutos; sin datos quedan
         * en null)
         */
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("porEstado", porEstado);
        resumen.put("cubiertosPorGarantia", servicios.stream().filter(ServicioTecnico::getCubiertoPorGarantia).count());
        resumen.put("entregados", entregados.size());
        resumen.put("pendientes", pendientes.size());
        resumen.put("tiempoAtencionMayor", tiempoMayor);
        resumen.put("tiempoAtencionMenor", tiempoMenor);
        resumen.put("tiempoAtencionPromedio", tiempoPromedio);
        resumen.put("antiguedadMayor", antiguedadMayor);
        resumen.put("antiguedadPromedio", antiguedadPromedio);

        return new ReporteResponse<>("Atenciones de servicio técnico", fechaInicio, fechaFin, filas.size(), resumen,
                filas);
    }

    /*
     * ==============================
     * REPORTE 5: REGISTROS ELIMINADOS (BAJAS LÓGICAS DE MERCADERÍA)
     * ==============================
     */

    /*
     * Una fila por cada baja registrada en la auditoría dentro del rango. Los
     * productos desactivados antes de
     * existir la auditoría no aparecen, porque no dejaron registro
     */
    public ReporteResponse<FilasReporte.EliminadoFila> eliminados(LocalDate fechaInicio, LocalDate fechaFin) {
        validarRango(fechaInicio, fechaFin);
        LocalDateTime inicio = inicioDelDia(fechaInicio);
        LocalDateTime fin = finDelDia(fechaFin);

        /*
         * Todo lo registrado desde el inicio del rango (sin tope): así se ven también
         * las reactivaciones posteriores
         */
        List<RegistroAuditoria> registros = auditoriaRepository.listarDesde(AuditoriaService.ENTIDAD_MERCADERIA,
                inicio);

        List<RegistroAuditoria> bajas = registros.stream()
                .filter(r -> r.getAccion() == AccionAuditoria.INACTIVACION && !r.getFechaHora().isAfter(fin))
                .toList();

        /* Productos involucrados, por SKU (una sola consulta) */
        List<String> skus = bajas.stream().map(RegistroAuditoria::getCodigoRegistro).distinct().toList();
        Map<String, Mercaderia> productos = new HashMap<>();
        if (!skus.isEmpty()) {
            for (Mercaderia m : mercaderiaRepository.findBySkuIn(skus)) {
                productos.put(m.getSku(), m);
            }
        }

        List<FilasReporte.EliminadoFila> filas = bajas.stream().map(baja -> {
            Mercaderia producto = productos.get(baja.getCodigoRegistro());

            /*
             * Primera reactivación del mismo producto posterior a esta baja (los registros
             * vienen en orden cronológico)
             */
            LocalDateTime fechaReactivacion = registros.stream()
                    .filter(r -> r.getAccion() == AccionAuditoria.REACTIVACION
                            && r.getCodigoRegistro().equals(baja.getCodigoRegistro())
                            && r.getFechaHora().isAfter(baja.getFechaHora()))
                    .map(RegistroAuditoria::getFechaHora)
                    .findFirst()
                    .orElse(null);

            return new FilasReporte.EliminadoFila(
                    baja.getFechaHora(),
                    baja.getCodigoRegistro(),
                    producto == null ? "(producto no encontrado)" : producto.getDescripcion(),
                    producto == null ? "-" : categoriaDe(producto),
                    AuditoriaService.categoriaDe(baja.getMotivo()).etiqueta(),
                    AuditoriaService.comentarioDe(baja.getMotivo()),
                    nombreTecnico(baja.getUsuario()),
                    producto == null ? "-" : (producto.getActivo() ? "Reactivado" : "Desactivado"),
                    producto == null ? null : producto.getStock(),
                    fechaReactivacion != null,
                    fechaReactivacion);
        }).toList();

        /*
         * Cuántas bajas hay por motivo (se cuentan bajas, todas las categorías aparecen
         * aunque tengan 0)
         */
        Map<String, Long> porMotivo = new LinkedHashMap<>();
        for (MotivoBaja motivo : MotivoBaja.values()) {
            porMotivo.put(motivo.name(), bajas.stream()
                    .filter(b -> AuditoriaService.categoriaDe(b.getMotivo()) == motivo).count());
        }

        /* Bajas cuyo producto todavía tiene unidades en stock */
        long conStockPendiente = filas.stream().filter(f -> f.getStock() != null && f.getStock() > 0).count();
        /*
         * Productos distintos con baja en el periodo que hoy están activos (coincide
         * con la columna ESTADO ACTUAL)
         */
        long reactivados = filas.stream()
                .filter(f -> "Reactivado".equals(f.getEstadoActual()))
                .map(f -> f.getSku())
                .distinct()
                .count();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalDesactivados", filas.size());
        resumen.put("porMotivo", porMotivo);
        resumen.put("conStockPendiente", conStockPendiente);
        resumen.put("reactivados", reactivados);
        resumen.put("productosDistintos", skus.size());

        return new ReporteResponse<>("Registros eliminados", fechaInicio, fechaFin, filas.size(), resumen, filas);
    }

    /*
     * ==============================
     * REPORTE 6: STOCK POR PRODUCTO (kardex resumido del periodo)
     * ==============================
     */

    public ReporteResponse<FilasReporte.StockFila> stock(LocalDate fechaInicio, LocalDate fechaFin) {
        validarRango(fechaInicio, fechaFin);
        LocalDateTime inicio = inicioDelDia(fechaInicio);
        LocalDateTime fin = finDelDia(fechaFin);

        /*
         * Regla B solo si el rango llega hasta hoy: si no, hubo movimientos posteriores
         * y el stock actual ya cambió
         */
        boolean comparaStockActual = !fechaFin.isBefore(LocalDate.now());

        /*
         * Todos los movimientos desde el inicio del rango, agrupados por producto
         * (siguen en orden cronológico)
         */
        Map<Integer, List<MovimientoInventario>> movimientosPorProducto = movimientoRepository
                .listarDesdeConMercaderia(inicio).stream()
                .collect(Collectors.groupingBy(m -> m.getMercaderia().getIdMercaderia()));

        List<FilasReporte.StockFila> filas = new ArrayList<>();
        for (Mercaderia producto : mercaderiaRepository.findByActivoTrue()) {
            List<MovimientoInventario> todos = movimientosPorProducto.getOrDefault(producto.getIdMercaderia(),
                    List.of());
            List<MovimientoInventario> enRango = todos.stream()
                    .filter(m -> !m.getFechaHora().isAfter(fin))
                    .toList();

            int stockInicial;
            int stockFinal;
            int entradas = 0;
            int salidas = 0;

            if (!enRango.isEmpty()) {
                /*
                 * Inicial = stock anterior del primer movimiento --- final = stock resultante
                 * del último
                 */
                stockInicial = enRango.get(0).getStockAnterior();
                stockFinal = enRango.get(enRango.size() - 1).getStockResultante();
                /*
                 * Entradas y salidas son unidades del MISMO producto, así que sí se pueden
                 * sumar
                 */
                for (MovimientoInventario m : enRango) {
                    if (m.getTipo().aumentaStock()) {
                        entradas += m.getCantidad();
                    } else {
                        salidas += m.getCantidad();
                    }
                }
            } else {
                /*
                 * Sin movimientos en el rango el stock no cambió. Si hubo movimientos después
                 * del rango,
                 * el stock anterior del primero dice con cuánto estaba; si no hubo ninguno, es
                 * el stock actual
                 */
                stockInicial = todos.isEmpty() ? producto.getStock() : todos.get(0).getStockAnterior();
                stockFinal = stockInicial;
            }

            /*
             * Regla A: lo que se movió debe explicar la diferencia entre el inicio y el
             * final
             */
            int diferenciaMovimientos = stockFinal - (stockInicial + entradas - salidas);
            /*
             * Regla B: el stock final del kardex debe coincidir con el stock actual del
             * producto
             */
            Integer diferenciaStockActual = comparaStockActual ? producto.getStock() - stockFinal : null;
            boolean descuadre = diferenciaMovimientos != 0
                    || (diferenciaStockActual != null && diferenciaStockActual != 0);

            /* Estado con el stock FINAL del periodo (no con el actual) */
            FilasReporte.EstadoStock estado;
            if (stockFinal <= 0) {
                estado = FilasReporte.EstadoStock.AGOTADO;
            } else if (stockFinal < producto.getStockMinimo()) {
                estado = FilasReporte.EstadoStock.BAJO_MINIMO;
            } else {
                estado = FilasReporte.EstadoStock.NORMAL;
            }

            filas.add(new FilasReporte.StockFila(
                    producto.getSku(),
                    producto.getDescripcion(),
                    categoriaDe(producto),
                    stockInicial,
                    entradas,
                    salidas,
                    stockFinal,
                    producto.getStockMinimo(),
                    estado,
                    diferenciaMovimientos,
                    diferenciaStockActual,
                    descuadre));
        }

        /* Por tipo y, dentro de cada tipo, por código */
        filas.sort(Comparator.comparing(FilasReporte.StockFila::getTipo)
                .thenComparing(FilasReporte.StockFila::getSku));

        /*
         * Producto con mayor caída: el que más bajó entre su stock inicial y el final
         * (si hay empate, el de menor código)
         */
        Destacado mayorCaida = filas.stream()
                .filter(f -> f.getStockInicial() - f.getStockFinal() > 0)
                .max(Comparator.comparingInt((FilasReporte.StockFila f) -> f.getStockInicial() - f.getStockFinal())
                        .thenComparing(FilasReporte.StockFila::getSku, Comparator.reverseOrder()))
                .map(f -> new Destacado(f.getSku() + " - " + f.getDescripcion(),
                        BigDecimal.valueOf(f.getStockInicial() - f.getStockFinal())))
                .orElse(null);

        /*
         * Bajo el mínimo y agotados no se mezclan: un producto agotado cuenta solo como
         * agotado
         */
        long bajoMinimo = filas.stream().filter(f -> f.getEstado() == FilasReporte.EstadoStock.BAJO_MINIMO).count();
        long agotados = filas.stream().filter(f -> f.getEstado() == FilasReporte.EstadoStock.AGOTADO).count();
        long conDescuadre = filas.stream().filter(FilasReporte.StockFila::getDescuadre).count();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalProductos", filas.size());
        resumen.put("mayorCaida", mayorCaida);
        resumen.put("bajoMinimo", bajoMinimo);
        resumen.put("agotados", agotados);
        resumen.put("conDescuadre", conDescuadre);
        resumen.put("comparaStockActual", comparaStockActual);

        return new ReporteResponse<>("Stock de productos", fechaInicio, fechaFin, filas.size(), resumen, filas);
    }

    /*
     * ==============================
     * MÉTODOS AUXILIARES
     * ==============================
     */

    /*
     * Las dos fechas son obligatorias y la de inicio no puede ser posterior a la de
     * fin
     */
    private void validarRango(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaNegocioException("Debe indicar fechaInicio y fechaFin con el formato AAAA-MM-DD.");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new ReglaNegocioException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }

    /* La fecha de inicio cuenta desde las 00:00:00 */
    private LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay();
    }

    /* La fecha de fin incluye todo ese día, hasta las 23:59:59 */
    private LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(23, 59, 59);
    }

    /* Si no hay cliente, fue una venta al paso */
    private String nombreCliente(Cliente cliente) {
        return cliente == null ? "Venta al paso" : cliente.getNombres() + " " + cliente.getApellidos();
    }

    /* Si no hay técnico asignado, se indica */
    private String nombreTecnico(Usuario tecnico) {
        return tecnico == null ? "Sin asignar" : tecnico.getNombres() + " " + tecnico.getApellidos();
    }

    /*
     * Nombre de una venta para mostrar en el reporte, por ejemplo
     * "BOLETA B001-0002"
     */
    private String nombreVenta(Venta venta) {
        return venta.getTipoComprobante().name() + " " + venta.getNumeroComprobante();
    }

    /* Categoría de un producto según su clase */
    private String categoriaDe(Mercaderia mercaderia) {
        if (mercaderia instanceof Scooter)
            return "Scooters";
        if (mercaderia instanceof Repuesto)
            return "Repuestos";
        if (mercaderia instanceof Accesorio)
            return "Accesorios";
        return "Otros";
    }

    /*
     * Producto con más (o con menos) unidades; si hay empate, gana el de menor
     * código
     */
    private Destacado productoExtremo(Map<String, Integer> unidadesPorSku, Map<String, String> nombrePorSku,
            boolean mayor) {
        return unidadesPorSku.entrySet().stream()
                .min((a, b) -> {
                    int c = mayor ? Integer.compare(b.getValue(), a.getValue())
                            : Integer.compare(a.getValue(), b.getValue());
                    return c != 0 ? c : a.getKey().compareTo(b.getKey());
                })
                .map(e -> new Destacado(nombrePorSku.get(e.getKey()), BigDecimal.valueOf(e.getValue())))
                .orElse(null);
    }

    /* Minutos que tardó la atención de un servicio entregado */
    private long minutosDeAtencion(ServicioTecnico servicio) {
        return Duration.between(servicio.getFechaIngreso(), servicio.getFechaEntrega()).toMinutes();
    }
}