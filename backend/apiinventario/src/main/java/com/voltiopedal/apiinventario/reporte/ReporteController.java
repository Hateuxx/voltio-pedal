package com.voltiopedal.apiinventario.reporte;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final ReportePdfService reportePdfService;

    /* Insertar dependencias por constructor */
    public ReporteController(ReporteService reporteService, ReportePdfService reportePdfService) {
        this.reporteService = reporteService;
        this.reportePdfService = reportePdfService;
    }

    /* GET /api/reportes/ventas?fechaInicio=2026-11-01&fechaFin=2026-11-30 */
    @GetMapping("/ventas")
    public ReporteResponse<FilasReporte.VentaDetalleFila> ventas(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return reporteService.ventas(fechaInicio, fechaFin);
    }

    /* GET /api/reportes/entradas?fechaInicio=...&fechaFin=... */
    @GetMapping("/entradas")
    public ReporteResponse<FilasReporte.MovimientoFila> entradas(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return reporteService.entradas(fechaInicio, fechaFin);
    }

    /* GET /api/reportes/salidas?fechaInicio=...&fechaFin=... */
    @GetMapping("/salidas")
    public ReporteResponse<FilasReporte.MovimientoFila> salidas(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return reporteService.salidas(fechaInicio, fechaFin);
    }

    /* GET /api/reportes/servicio-tecnico?fechaInicio=...&fechaFin=... */
    @GetMapping("/servicio-tecnico")
    public ReporteResponse<FilasReporte.ServicioFila> servicioTecnico(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return reporteService.servicioTecnico(fechaInicio, fechaFin);
    }

    /*
     * GET /api/reportes/ventas/pdf?fechaInicio=...&fechaFin=... -> descarga el PDF
     */
    @GetMapping("/ventas/pdf")
    public ResponseEntity<byte[]> ventasPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        byte[] pdf = reportePdfService.ventasPdf(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_ventas.pdf\"")
                .body(pdf);
    }

    /* GET /api/reportes/entradas/pdf?fechaInicio=...&fechaFin=... -> descarga el PDF */
    @GetMapping("/entradas/pdf")
    public ResponseEntity<byte[]> entradasPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        byte[] pdf = reportePdfService.entradasPdf(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_entradas.pdf\"")
                .body(pdf);
    }

    /* GET /api/reportes/salidas/pdf?fechaInicio=...&fechaFin=... -> descarga el PDF */
    @GetMapping("/salidas/pdf")
    public ResponseEntity<byte[]> salidasPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        byte[] pdf = reportePdfService.salidasPdf(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_salidas.pdf\"")
                .body(pdf);
    }

    /* GET /api/reportes/servicio-tecnico/pdf?fechaInicio=...&fechaFin=... -> descarga el PDF */
    @GetMapping("/servicio-tecnico/pdf")
    public ResponseEntity<byte[]> servicioTecnicoPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        byte[] pdf = reportePdfService.servicioTecnicoPdf(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_servicio_tecnico.pdf\"")
                .body(pdf);
    }

    /* GET /api/reportes/eliminados?fechaInicio=...&fechaFin=... */
    @GetMapping("/eliminados")
    public ReporteResponse<FilasReporte.EliminadoFila> eliminados(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return reporteService.eliminados(fechaInicio, fechaFin);
    }

    /* GET /api/reportes/eliminados/pdf?fechaInicio=...&fechaFin=... -> descarga el PDF */
    @GetMapping("/eliminados/pdf")
    public ResponseEntity<byte[]> eliminadosPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        byte[] pdf = reportePdfService.eliminadosPdf(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_eliminados.pdf\"")
                .body(pdf);
    }

    /* GET /api/reportes/stock?fechaInicio=...&fechaFin=... */
    @GetMapping("/stock")
    public ReporteResponse<FilasReporte.StockFila> stock(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return reporteService.stock(fechaInicio, fechaFin);
    }

    /* GET /api/reportes/stock/pdf?fechaInicio=...&fechaFin=... -> descarga el PDF */
    @GetMapping("/stock/pdf")
    public ResponseEntity<byte[]> stockPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        byte[] pdf = reportePdfService.stockPdf(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_stock.pdf\"")
                .body(pdf);
    }
}