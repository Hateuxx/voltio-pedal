package com.voltiopedal.apiinventario.reporte;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.voltiopedal.apiinventario.movimiento.TipoMovimiento;
import java.util.Comparator;
import com.voltiopedal.apiinventario.serviciotecnico.EstadoServicio;
import com.voltiopedal.apiinventario.auditoria.MotivoBaja;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigDecimal;
import java.util.Locale;

/* Genera los reportes en PDF usando las plantillas de Jaspersoft Studio (carpeta resources/reportes) */
@Service
public class ReportePdfService {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReporteService reporteService;

    /* Plantillas ya compiladas: se compilan la primera vez que se usan y luego se reutilizan */
    private final Map<String, JasperReport> plantillas = new ConcurrentHashMap<>();

    /* Insertar dependencia por constructor */
    public ReportePdfService(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /* ==============================
     *     REPORTE DE VENTAS EN PDF
     * ============================== */

    public byte[] ventasPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        /* Reporte de ventas con una fila por producto vendido */
        ReporteResponse<FilasReporte.VentaDetalleFila> reporte = reporteService.ventas(fechaInicio, fechaFin);

        /* Cada línea se convierte en un mapa: nombre del campo -> valor  */
        List<Map<String, ?>> filas = new ArrayList<>();
        for (FilasReporte.VentaDetalleFila d : reporte.filas()) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("fecha", d.getFecha().format(FORMATO_FECHA_HORA));
            fila.put("tipoComprobante", d.getTipoComprobante().name());
            fila.put("numeroComprobante", d.getNumeroComprobante());
            fila.put("cliente", d.getCliente());
            fila.put("sku", d.getSku());
            fila.put("descripcion", d.getDescripcion());
            fila.put("cantidad", d.getCantidad());
            fila.put("precioUnitario", d.getPrecioUnitario());
            fila.put("subtotal", d.getSubtotal());
            /* Total de toda la venta (se repite en cada producto de la misma venta) */
            fila.put("total", d.getTotalVenta());
            filas.add(fila);
        }

                /* Parámetros de la plantilla: título, fechas, totales y estadísticas */
        Map<String, Object> resumen = reporte.resumen();
        Map<String, Object> parametros = parametrosBase(reporte);
        parametros.put("totalRegistros", comoEntero(resumen.get("totalVentas")));
        parametros.put("montoTotal", resumen.get("montoTotal"));
        parametros.put("cantidadBoletas", comoEntero(resumen.get("cantidadBoletas")));
        parametros.put("cantidadFacturas", comoEntero(resumen.get("cantidadFacturas")));
        parametros.put("ventaMayor", textoDinero((Destacado) resumen.get("ventaMayor")));
        parametros.put("ventaMenor", textoDinero((Destacado) resumen.get("ventaMenor")));
        BigDecimal ticket = (BigDecimal) resumen.get("ticketPromedio");
        parametros.put("ticketPromedio", ticket == null ? "-" : "S/ " + dinero(ticket));
        parametros.put("productoMasVendido", textoUnidades((Destacado) resumen.get("productoMasVendido")));
        parametros.put("productoMenosVendido", textoUnidades((Destacado) resumen.get("productoMenosVendido")));
        parametros.put("categoriaMasVendida", textoDinero((Destacado) resumen.get("categoriaMasVendida")));
        parametros.put("categoriaMenosVendida", textoDinero((Destacado) resumen.get("categoriaMenosVendida")));
        return generarPdf("reporte_ventas.jrxml", parametros, filas);
    }
    
    /* ==============================
     *   ENTRADAS Y SALIDAS EN PDF 
     * ============================== */

    public byte[] entradasPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        return movimientosPdf(reporteService.entradas(fechaInicio, fechaFin));
    }

    public byte[] salidasPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        return movimientosPdf(reporteService.salidas(fechaInicio, fechaFin));
    }


    private byte[] movimientosPdf(ReporteResponse<FilasReporte.MovimientoFila> reporte) {
        /* La plantilla agrupa por tipo: las filas deben llegar ordenadas por tipo y, dentro de cada tipo, de la más reciente a la más antigua */
        List<FilasReporte.MovimientoFila> ordenadas = reporte.filas().stream()
                .sorted(Comparator.comparing((FilasReporte.MovimientoFila m) -> m.getTipo().ordinal())
                        .thenComparing(FilasReporte.MovimientoFila::getFecha, Comparator.reverseOrder()))
                .toList();

        /* Cada movimiento se convierte en un mapa: nombre del campo -> valor (los mismos nombres de la plantilla) */
        List<Map<String, ?>> filas = new ArrayList<>();
        for (FilasReporte.MovimientoFila m : ordenadas) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("fecha", m.getFecha().format(FORMATO_FECHA_HORA));
            fila.put("tipo", etiquetaTipo(m.getTipo()));
            fila.put("sku", m.getSku());
            fila.put("descripcion", m.getDescripcion());
            fila.put("cantidad", m.getCantidad());
            fila.put("stockAnterior", m.getStockAnterior());
            fila.put("stockResultante", m.getStockResultante());
            fila.put("observacion", m.getObservacion());
            filas.add(fila);
        }

        /* Parámetros de la plantilla: título, fechas, totales y estadísticas */
        Map<String, Object> resumen = reporte.resumen();
        Map<String, Object> parametros = parametrosBase(reporte);
        parametros.put("totalRegistros", comoEntero(resumen.get("totalMovimientos")));
        parametros.put("productosConMovimiento", comoEntero(resumen.get("productosConMovimiento")));
        parametros.put("productoMayor", textoUnidades((Destacado) resumen.get("productoMayor")));
        parametros.put("productoMenor", textoUnidades((Destacado) resumen.get("productoMenor")));
        parametros.put("movimientoMayor", textoUnidades((Destacado) resumen.get("movimientoMayor")));
        parametros.put("movimientoMenor", textoUnidades((Destacado) resumen.get("movimientoMenor")));
        BigDecimal promedio = (BigDecimal) resumen.get("promedioPorMovimiento");
        parametros.put("promedioMovimiento", promedio == null ? "-" : String.format(Locale.US, "%.1f", promedio) + " u.");
        parametros.put("productosSinMovimiento", resumen.get("productosSinMovimiento") + " de " + resumen.get("totalProductosActivos"));

        return generarPdf("reporte_movimientos.jrxml", parametros, filas);
    }

    /* Nombre legible de cada tipo de movimiento, tal como se muestra en el reporte */
    private String etiquetaTipo(TipoMovimiento tipo) {
        return switch (tipo) {
            case ENTRADA_PROVEEDOR -> "Compra a proveedor";
            case DEVOLUCION_PROVEEDOR -> "Devolución a proveedor";
            case SALIDA_VENTA -> "Venta";
            case DEVOLUCION_VENTA -> "Devolución de venta";
            case SALIDA_REPARACION -> "Repuesto usado en reparación";
            case DEVOLUCION_REPARACION -> "Devolución de reparación";
            case AJUSTE_ENTRADA -> "Ajuste de entrada";
            case AJUSTE_SALIDA -> "Ajuste de salida";
        };
    }

        /* ==============================
     *   ATENCIONES DE SERVICIO TÉCNICO EN PDF
     * ============================== */

    public byte[] servicioTecnicoPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        ReporteResponse<FilasReporte.ServicioFila> reporte = reporteService.servicioTecnico(fechaInicio, fechaFin);

        /* La plantilla agrupa por estado: las filas se ordenan por estado y, dentro de cada uno, de la más reciente a la más antigua */
        List<FilasReporte.ServicioFila> ordenadas = reporte.filas().stream()
                .sorted(Comparator.comparing((FilasReporte.ServicioFila s) -> s.getEstado().ordinal())
                        .thenComparing(FilasReporte.ServicioFila::getFechaIngreso, Comparator.reverseOrder()))
                .toList();

        /* Cada servicio se convierte en un mapa: nombre del campo -> valor (los mismos nombres de la plantilla) */
        List<Map<String, ?>> filas = new ArrayList<>();
        for (FilasReporte.ServicioFila s : ordenadas) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("fechaIngreso", s.getFechaIngreso().format(FORMATO_FECHA_HORA));
            fila.put("fechaEntrega", s.getFechaEntrega() != null ? s.getFechaEntrega().format(FORMATO_FECHA_HORA) : "-");
            fila.put("cliente", s.getCliente());
            fila.put("scooter", s.getScooter());
            fila.put("numeroSerie", s.getNumeroSerie());
            fila.put("fallaReportada", s.getFallaReportada());
            fila.put("estado", etiquetaEstado(s.getEstado()));
            fila.put("tecnico", s.getTecnico());
            fila.put("garantia", Boolean.TRUE.equals(s.getCubiertoPorGarantia()) ? "Sí" : "No");
            filas.add(fila);
        }

        /* Totales: cuántos servicios hay en total, entregados, en curso y cubiertos por garantía */
        Map<?, ?> porEstado = (Map<?, ?>) reporte.resumen().get("porEstado");
        int entregados = comoEntero(porEstado.get(EstadoServicio.ENTREGADO.name()));

        Map<String, Object> parametros = parametrosBase(reporte);
        parametros.put("totalRegistros", reporte.totalRegistros());
        parametros.put("totalEntregados", entregados);
        parametros.put("totalEnCurso", reporte.totalRegistros() - entregados);
        parametros.put("cubiertosPorGarantia", comoEntero(reporte.resumen().get("cubiertosPorGarantia")));
                /* Estadísticas de tiempos (en el PDF se muestran como días, horas y minutos) */
        Map<String, Object> resumen = reporte.resumen();
        parametros.put("tiempoMayor", textoTiempo((Destacado) resumen.get("tiempoAtencionMayor")));
        parametros.put("tiempoMenor", textoTiempo((Destacado) resumen.get("tiempoAtencionMenor")));
        parametros.put("tiempoPromedio", textoPromedioTiempo((BigDecimal) resumen.get("tiempoAtencionPromedio")));
        parametros.put("antiguedadMayor", textoTiempo((Destacado) resumen.get("antiguedadMayor")));
        parametros.put("antiguedadPromedio", textoPromedioTiempo((BigDecimal) resumen.get("antiguedadPromedio")));

        return generarPdf("reporte_servicio_tecnico.jrxml", parametros, filas);
    }

    /* Nombre legible de cada estado de servicio, tal como se muestra en el reporte */
    private String etiquetaEstado(EstadoServicio estado) {
        return switch (estado) {
            case INGRESADO -> "Ingresado";
            case EN_REPARACION -> "En reparación";
            case ESPERANDO_REPUESTO -> "Esperando repuesto";
            case LISTO_PARA_ENTREGA -> "Listo para entrega";
            case ENTREGADO -> "Entregado";
        };
    }

    /* ==============================
     *   REGISTROS ELIMINADOS EN PDF
     * ============================== */

    public byte[] eliminadosPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        ReporteResponse<FilasReporte.EliminadoFila> reporte = reporteService.eliminados(fechaInicio, fechaFin);

        /* La plantilla agrupa por motivo: las filas se ordenan por motivo y, dentro de cada uno, de la más reciente a la más antigua */
        List<FilasReporte.EliminadoFila> ordenadas = reporte.filas().stream()
                .sorted(Comparator.comparing(FilasReporte.EliminadoFila::getMotivo)
                        .thenComparing(FilasReporte.EliminadoFila::getFechaBaja, Comparator.reverseOrder()))
                .toList();

        /* Cada baja se convierte en un mapa: nombre del campo -> valor (los mismos nombres de la plantilla) */
        List<Map<String, ?>> filas = new ArrayList<>();
        for (FilasReporte.EliminadoFila e : ordenadas) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("fechaBaja", e.getFechaBaja().format(FORMATO_FECHA_HORA));
            fila.put("sku", e.getSku());
            fila.put("descripcion", e.getDescripcion());
            fila.put("categoria", e.getCategoria());
            fila.put("motivo", e.getMotivo());
            fila.put("comentario", e.getComentario());
            fila.put("usuario", e.getUsuario());
            fila.put("estadoActual", e.getEstadoActual());
            fila.put("stock", e.getStock());
            filas.add(fila);
        }

        /* Parámetros de la plantilla: título, fechas, totales y desglose por motivo */
        Map<String, Object> resumen = reporte.resumen();
        Map<?, ?> porMotivo = (Map<?, ?>) resumen.get("porMotivo");

        Map<String, Object> parametros = parametrosBase(reporte);
        parametros.put("totalRegistros", reporte.totalRegistros());
        parametros.put("conStockPendiente", comoEntero(resumen.get("conStockPendiente")));
        parametros.put("reactivados", comoEntero(resumen.get("reactivados")));
        parametros.put("productosDistintos", comoEntero(resumen.get("productosDistintos")));
        parametros.put("motivoBajaRotacion", comoEntero(porMotivo.get(MotivoBaja.BAJA_ROTACION.name())));
        parametros.put("motivoDefecto", comoEntero(porMotivo.get(MotivoBaja.DEFECTO.name())));
        parametros.put("motivoDescontinuado", comoEntero(porMotivo.get(MotivoBaja.DESCONTINUADO.name())));
        parametros.put("motivoErrorRegistro", comoEntero(porMotivo.get(MotivoBaja.ERROR_REGISTRO.name())));
        parametros.put("motivoOtro", comoEntero(porMotivo.get(MotivoBaja.OTRO.name())));

        return generarPdf("reporte_eliminados.jrxml", parametros, filas);
    }

    /* ==============================
     *   STOCK DE PRODUCTOS EN PDF
     * ============================== */

    public byte[] stockPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        ReporteResponse<FilasReporte.StockFila> reporte = reporteService.stock(fechaInicio, fechaFin);

        /* Las filas ya vienen ordenadas por tipo y, dentro de cada tipo, por código: así las necesita la plantilla */
        List<Map<String, ?>> filas = new ArrayList<>();
        for (FilasReporte.StockFila s : reporte.filas()) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("sku", s.getSku());
            fila.put("descripcion", s.getDescripcion());
            fila.put("tipo", s.getTipo());
            fila.put("stockInicial", s.getStockInicial());
            fila.put("entradas", s.getEntradas());
            fila.put("salidas", s.getSalidas());
            fila.put("stockFinal", s.getStockFinal());
            fila.put("stockMinimo", s.getStockMinimo());
            fila.put("estado", etiquetaEstadoStock(s.getEstado()));
            fila.put("descuadre", Boolean.TRUE.equals(s.getDescuadre()) ? "Sí" : "No");
            filas.add(fila);
        }

        /* Parámetros de la plantilla: título, fechas, totales e indicadores */
        Map<String, Object> resumen = reporte.resumen();
        int total = comoEntero(resumen.get("totalProductos"));
        int bajoMinimo = comoEntero(resumen.get("bajoMinimo"));
        int agotados = comoEntero(resumen.get("agotados"));

        Map<String, Object> parametros = parametrosBase(reporte);
        parametros.put("totalRegistros", total);
        parametros.put("mayorCaida", textoUnidades((Destacado) resumen.get("mayorCaida")));
        parametros.put("bajoMinimo", bajoMinimo);
        parametros.put("agotados", agotados);
        parametros.put("conDescuadre", comoEntero(resumen.get("conDescuadre")));
        /* Los demás: ni agotados ni bajo el mínimo */
        parametros.put("normales", total - bajoMinimo - agotados);
        parametros.put("comparaStockActual", Boolean.TRUE.equals(resumen.get("comparaStockActual")) ? "Sí" : "No");

        return generarPdf("reporte_stock.jrxml", parametros, filas);
    }

    /* Nombre legible de cada estado de stock, tal como se muestra en el reporte */
    private String etiquetaEstadoStock(FilasReporte.EstadoStock estado) {
        return switch (estado) {
            case NORMAL -> "Normal";
            case BAJO_MINIMO -> "Bajo el mínimo";
            case AGOTADO -> "Agotado";
        };
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* Parámetros que tienen todos los reportes: título, rango de fechas y fecha de emisión */
    private Map<String, Object> parametrosBase(ReporteResponse<?> reporte) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("titulo", reporte.titulo().toUpperCase());
        parametros.put("fechaInicio", reporte.fechaInicio().format(FORMATO_FECHA));
        parametros.put("fechaFin", reporte.fechaFin().format(FORMATO_FECHA));
        parametros.put("fechaEmision", LocalDateTime.now().format(FORMATO_FECHA_HORA));
        return parametros;
    }

    /* Los conteos llegan como Long y la plantilla espera Integer */
    private Integer comoEntero(Object valor) {
        return ((Number) valor).intValue();
    }

    /* Carga la plantilla, la llena con los datos y la exporta a PDF */
    private byte[] generarPdf(String plantilla, Map<String, Object> parametros, List<Map<String, ?>> filas) {
        try {
            JasperPrint impreso = JasperFillManager.fillReport(
                    cargarPlantilla(plantilla), parametros, new JRMapCollectionDataSource(filas));

            JRPdfExporter exportador = new JRPdfExporter();
            exportador.setExporterInput(new SimpleExporterInput(impreso));
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            exportador.setExporterOutput(new SimpleOutputStreamExporterOutput(salida));
            exportador.exportReport();
            return salida.toByteArray();
        } catch (JRException e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte " + plantilla, e);
        }
    }

    /* Busca la plantilla en resources/reportes y la compila (una sola vez) */
    private JasperReport cargarPlantilla(String nombreArchivo) {
        return plantillas.computeIfAbsent(nombreArchivo, nombre -> {
            try (InputStream entrada = new ClassPathResource("reportes/" + nombre).getInputStream()) {
                return JasperCompileManager.compileReport(entrada);
            } catch (IOException | JRException e) {
                throw new IllegalStateException("No se pudo cargar la plantilla " + nombre, e);
            }
        });
    }

        /* Texto "nombre (S/ monto)" para las estadísticas en dinero; "-" si no hay datos */
    private String textoDinero(Destacado destacado) {
        return destacado == null ? "-" : destacado.nombre() + " (S/ " + dinero(destacado.valor()) + ")";
    }

    /* Texto "código (n u.)" para las estadísticas en unidades; "-" si no hay datos */
    private String textoUnidades(Destacado destacado) {
        return destacado == null ? "-" : codigo(destacado.nombre()) + " (" + destacado.valor().intValue() + " u.)";
    }

    /* Monto con separador de miles y dos decimales, por ejemplo 1,250.00 */
    private String dinero(BigDecimal valor) {
        return String.format(Locale.US, "%,.2f", valor);
    }

    /* "SCO-001 - Scooter..." -> "SCO-001" (el PDF muestra solo el código para que quepa) */
    private String codigo(String nombre) {
        int posicion = nombre.indexOf(" - ");
        return posicion > 0 ? nombre.substring(0, posicion) : nombre;
    }

        /* Texto "número de serie (2 d 4 h)" para los tiempos; "-" si no hay datos */
    private String textoTiempo(Destacado destacado) {
        return destacado == null ? "-" : destacado.nombre() + " (" + duracion(destacado.valor().longValue()) + ")";
    }

    /* Texto del promedio, por ejemplo "5 h 12 min"; "-" si no hay datos */
    private String textoPromedioTiempo(BigDecimal minutos) {
        return minutos == null ? "-" : duracion(minutos.longValue());
    }

    /* Convierte minutos a "2 d 4 h", "5 h 12 min" o "8 min", según el tamaño */
    private String duracion(long minutosTotales) {
        long dias = minutosTotales / 1440;
        long horas = (minutosTotales % 1440) / 60;
        long minutos = minutosTotales % 60;
        if (dias > 0) {
            return dias + " d " + horas + " h";
        }
        if (horas > 0) {
            return horas + " h " + minutos + " min";
        }
        return minutos + " min";
    }
}
