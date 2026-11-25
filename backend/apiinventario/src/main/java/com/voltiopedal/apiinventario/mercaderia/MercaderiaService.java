package com.voltiopedal.apiinventario.mercaderia;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.common.ValidacionException;
import com.voltiopedal.apiinventario.proveedor.Proveedor;
import com.voltiopedal.apiinventario.proveedor.ProveedorRepository;
import com.voltiopedal.apiinventario.tiporepuesto.TipoRepuesto;
import com.voltiopedal.apiinventario.tiporepuesto.TipoRepuestoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MercaderiaService {
    private final MercaderiaRepository mercaderiaRepository;
    private final ProveedorRepository proveedorRepository;
    private final TipoRepuestoRepository tipoRepuestoRepository;

    public MercaderiaService(MercaderiaRepository mercaderiaRepository,
                             ProveedorRepository proveedorRepository,
                             TipoRepuestoRepository tipoRepuestoRepository) {
        this.mercaderiaRepository = mercaderiaRepository;
        this.proveedorRepository = proveedorRepository;
        this.tipoRepuestoRepository = tipoRepuestoRepository;
    }

    /* ==============================
     *          C - CREAR
     * ============================== */

    @Transactional
    public MercaderiaResponse registrar(MercaderiaRequest request) {
        String sku = normalizarSku(request.sku());
        if (mercaderiaRepository.existsBySku(sku)) {
            throw new ReglaNegocioException("Ya existe una mercadería con el SKU: " + sku);
        }

        Mercaderia mercaderia = crearSegunTipo(request.tipo());
        aplicarDatos(mercaderia, request, sku);
        mercaderia.setStock(0);                          /* el stock nace en 0 */
        mercaderia.setFechaIngreso(LocalDateTime.now());

        return aResponse(mercaderiaRepository.save(mercaderia));
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    @Transactional(readOnly = true)
    public List<MercaderiaResponse> listarActivas() {
        return mercaderiaRepository.findByActivoTrueOrderByDescripcionAsc().stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MercaderiaResponse> listarInactivas() {
        return mercaderiaRepository.findByActivoFalseOrderByDescripcionAsc().stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public MercaderiaResponse buscarPorId(Integer id) {
        return aResponse(obtenerPorId(id));
    }

    @Transactional(readOnly = true)
    public List<MercaderiaResponse> listarStockBajo() {
        return mercaderiaRepository.listarConStockBajo().stream().map(this::aResponse).toList();
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    @Transactional
    public MercaderiaResponse actualizar(Integer id, MercaderiaRequest request) {
        Mercaderia mercaderia = obtenerPorId(id);

        if (!tipoDe(mercaderia).equals(request.tipo())) {
            throw new ReglaNegocioException("No se puede cambiar el tipo de una mercadería ya registrada.");
        }

        String sku = normalizarSku(request.sku());
        if (mercaderiaRepository.existsBySkuAndIdMercaderiaNot(sku, id)) {
            throw new ReglaNegocioException("Ya existe otra mercadería con el SKU: " + sku);
        }

        aplicarDatos(mercaderia, request, sku);
        return aResponse(mercaderia);   /* dirty checking: JPA guarda los cambios al terminar la transacción */
    }

    /* ==============================
     *   D - BAJA LÓGICA / REACTIVAR
     * ============================== */

    @Transactional
    public void desactivar(Integer id) {
        Mercaderia mercaderia = obtenerPorId(id);
        if (!mercaderia.getActivo()) {
            throw new ReglaNegocioException("La mercadería ya está dada de baja.");
        }
        mercaderia.setActivo(false);
    }

    @Transactional
    public void reactivar(Integer id) {
        Mercaderia mercaderia = obtenerPorId(id);
        if (mercaderia.getActivo()) {
            throw new ReglaNegocioException("La mercadería ya está activa.");
        }
        mercaderia.setActivo(true);
    }

    /* ==============================
     *       D - ELIMINAR FÍSICO
     * ============================== */

    @Transactional
    public void eliminarFisico(Integer id) {
        Mercaderia mercaderia = obtenerPorId(id);
        try {
            mercaderiaRepository.delete(mercaderia);
            mercaderiaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException("No se puede eliminar la mercadería porque tiene movimientos asociados (ingresos, ventas o servicios). Use 'Dar de baja'.");
        }
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    private Mercaderia obtenerPorId(Integer id) {
        return mercaderiaRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + id));
    }

    private String normalizarSku(String sku) {
        return sku.trim().toUpperCase();
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    /* Fábrica: crea la subclase correcta según el texto del tipo */
    private Mercaderia crearSegunTipo(String tipo) {
        return switch (tipo) {
            case "SCOOTER" -> new Scooter();
            case "REPUESTO" -> new Repuesto();
            case "ACCESORIO" -> new Accesorio();
            default -> throw new ReglaNegocioException("Tipo de mercadería no válido: " + tipo);
        };
    }

    /* Pattern matching (Java 21+): pregunta "¿de qué clase es este objeto?" */
    private String tipoDe(Mercaderia m) {
        return switch (m) {
            case Scooter _ -> "SCOOTER";
            case Repuesto _ -> "REPUESTO";
            case Accesorio _ -> "ACCESORIO";
            default -> throw new IllegalStateException("Tipo de mercadería desconocido");
        };
    }

    /* Valida según el tipo y copia los datos del Request a la entidad (sirve para crear y editar) */
    private void aplicarDatos(Mercaderia m, MercaderiaRequest r, String sku) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (r.precioVenta().compareTo(r.precioCosto()) < 0) {
            errores.put("precioVenta", "El precio de venta no puede ser menor que el precio de costo.");
        }

        switch (m) {
            case Scooter _ -> {
                if (r.autonomiaKm() == null) errores.put("autonomiaKm", "La autonomía es obligatoria para un scooter.");
                if (r.potenciaWatts() == null) errores.put("potenciaWatts", "La potencia es obligatoria para un scooter.");
                if (vacioANull(r.color()) == null) errores.put("color", "El color es obligatorio para un scooter.");
                if (r.mesesGarantia() == null) errores.put("mesesGarantia", "La garantía es obligatoria para un scooter.");
            }
            case Repuesto _ -> {
                if (r.idTipoRepuesto() == null) errores.put("idTipoRepuesto", "El tipo de repuesto es obligatorio.");
                if (vacioANull(r.modelosCompatibles()) == null) errores.put("modelosCompatibles", "Los modelos compatibles son obligatorios.");
            }
            default -> { /* ACCESORIO: talla y color son opcionales */ }
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        Proveedor proveedor = proveedorRepository.findById(r.idProveedor()).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún proveedor con el id: " + r.idProveedor()));

        /* Un proveedor dado de baja no puede asignarse (pero si ya era el proveedor, se permite editar lo demás) */
        boolean cambiaProveedor = m.getProveedor() == null
                || !m.getProveedor().getIdProveedor().equals(proveedor.getIdProveedor());
        if (cambiaProveedor && !proveedor.getActivo()) {
            throw new ReglaNegocioException("El proveedor seleccionado está dado de baja.");
        }

        m.setSku(sku);
        m.setDescripcion(r.descripcion().trim());
        m.setStockMinimo(r.stockMinimo());
        m.setPrecioCosto(r.precioCosto());
        m.setPrecioVenta(r.precioVenta());
        m.setProveedor(proveedor);

        switch (m) {
            case Scooter s -> {
                s.setAutonomiaKm(r.autonomiaKm());
                s.setPotenciaWatts(r.potenciaWatts());
                s.setColor(r.color().trim());
                s.setMesesGarantia(r.mesesGarantia());
            }
            case Repuesto rep -> {
                TipoRepuesto tipo = tipoRepuestoRepository.findById(r.idTipoRepuesto()).orElseThrow(
                        () -> new RecursoNoEncontradoException("No se encontró ningún tipo de repuesto con el id: " + r.idTipoRepuesto()));
                rep.setTipoRepuesto(tipo);
                rep.setModelosCompatibles(r.modelosCompatibles().trim());
            }
            case Accesorio a -> {
                a.setTalla(vacioANull(r.talla()));
                a.setColor(vacioANull(r.color()));
            }
            default -> throw new IllegalStateException("Tipo de mercadería desconocido");
        }
    }

    /* Entidad -> Response: los campos que no aplican al tipo quedan en null */
    private MercaderiaResponse aResponse(Mercaderia m) {
        Integer autonomiaKm = null, potenciaWatts = null, mesesGarantia = null, idTipoRepuesto = null;
        String color = null, tipoRepuesto = null, modelosCompatibles = null, talla = null;

        switch (m) {
            case Scooter s -> {
                autonomiaKm = s.getAutonomiaKm();
                potenciaWatts = s.getPotenciaWatts();
                color = s.getColor();
                mesesGarantia = s.getMesesGarantia();
            }
            case Repuesto rep -> {
                idTipoRepuesto = rep.getTipoRepuesto().getIdTipoRepuesto();
                tipoRepuesto = rep.getTipoRepuesto().getNombre();
                modelosCompatibles = rep.getModelosCompatibles();
            }
            case Accesorio a -> {
                talla = a.getTalla();
                color = a.getColor();
            }
            default -> { }
        }

        return new MercaderiaResponse(
                m.getIdMercaderia(), tipoDe(m), m.getSku(), m.getDescripcion(),
                m.getStock(), m.getStockMinimo(), m.getPrecioCosto(), m.getPrecioVenta(),
                m.getFechaIngreso(), m.getActivo(),
                m.getProveedor().getIdProveedor(), m.getProveedor().getRazonSocial(),
                autonomiaKm, potenciaWatts, color, mesesGarantia,
                idTipoRepuesto, tipoRepuesto, modelosCompatibles,
                talla);
    }
}