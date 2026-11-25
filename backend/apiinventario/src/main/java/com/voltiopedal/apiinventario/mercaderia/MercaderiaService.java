package com.voltiopedal.apiinventario.mercaderia;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.proveedor.Proveedor;
import com.voltiopedal.apiinventario.proveedor.ProveedorRepository;
import com.voltiopedal.apiinventario.tiporepuesto.TipoRepuesto;
import com.voltiopedal.apiinventario.tiporepuesto.TipoRepuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.voltiopedal.apiinventario.auditoria.AccionAuditoria;
import com.voltiopedal.apiinventario.auditoria.AuditoriaService;
import com.voltiopedal.apiinventario.auditoria.MotivoBaja;

import java.util.List;
import java.util.Optional;

@Service
public class MercaderiaService {
    private final MercaderiaRepository mercaderiaRepository;
    private final ProveedorRepository proveedorRepository;
    private final TipoRepuestoRepository tipoRepuestoRepository;
    private final AuditoriaService auditoriaService;

    /* Insertar dependencias por constructor (ahora también proveedor, tipo de repuesto y auditoría) */
    public MercaderiaService(MercaderiaRepository mercaderiaRepository,
                             ProveedorRepository proveedorRepository,
                             TipoRepuestoRepository tipoRepuestoRepository,
                             AuditoriaService auditoriaService) {
        this.mercaderiaRepository = mercaderiaRepository;
        this.proveedorRepository = proveedorRepository;
        this.tipoRepuestoRepository = tipoRepuestoRepository;
        this.auditoriaService = auditoriaService;
    }
    /* ==============================
     *          C - CREAR
     * ============================== */

    /* @Transactional: o se guarda todo (producto y datos de su tipo), o se revierte todo */
    @Transactional
    public MercaderiaResponse registrar(MercaderiaRequest request) {
        /* 1. El SKU no puede estar repetido */
        Optional<Mercaderia> existente = mercaderiaRepository.findBySku(request.sku());
        if (existente.isPresent()) {
            if (existente.get().getActivo()) {
                throw new ReglaNegocioException("Ya existe una mercadería registrada con el SKU: " + request.sku());
            }
            throw new ReglaNegocioException("La mercadería con SKU " + request.sku() + " existe, pero se encuentra DESACTIVADA.");
        }

        /* 2. Validaciones propias del tipo y del proveedor */
        validarDatosDelTipo(request);
        Proveedor proveedor = obtenerProveedorActivo(request.idProveedor());

        /* 3. Se crea el objeto de la clase hija que corresponde y se llenan sus datos */
        Mercaderia nueva = crearVacia(request.tipo());
        copiarDatosComunes(nueva, request, proveedor);
        copiarDatosDelTipo(nueva, request);

        return aResponse(mercaderiaRepository.save(nueva));
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* Lista la mercadería activa con los datos de su proveedor */
    public List<MercaderiaResponse> listarActivas() {
        return mercaderiaRepository.listarActivasConProveedor().stream().map(this::aResponse).toList();
    }

    /* Busca una mercadería por su id (también si está desactivada; en ese caso incluye el motivo de la baja) */
    public MercaderiaResponse buscarPorId(Integer id) {
        Mercaderia mercaderia = mercaderiaRepository.buscarPorIdConProveedor(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + id));

        MercaderiaResponse.InfoBaja baja = null;
        if (!mercaderia.getActivo()) {
            baja = auditoriaService.ultimaBaja(AuditoriaService.ENTIDAD_MERCADERIA, mercaderia.getSku())
                    .map(r -> new MercaderiaResponse.InfoBaja(
                            AuditoriaService.categoriaDe(r.getMotivo()).etiqueta(),
                            AuditoriaService.comentarioDe(r.getMotivo()),
                            r.getFechaHora(),
                            r.getUsuario().getNombres() + " " + r.getUsuario().getApellidos()))
                    .orElse(null);
        }
        return construirResponse(mercaderia, baja);
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    /* ACTUALIZAR: no toca el id, el stock ni el estado activo */
    @Transactional
    public MercaderiaResponse actualizar(Integer id, MercaderiaRequest request) {
        Mercaderia mercaderia = obtenerPorId(id);

        /* El tipo no se puede cambiar: cada tipo guarda sus datos en una tabla distinta */
        if (tipoDe(mercaderia) != request.tipo()) {
            throw new ReglaNegocioException("No se puede cambiar el tipo de una mercadería ya registrada (es " + tipoDe(mercaderia) + ").");
        }

        /* El SKU no puede coincidir con el de otro producto */
        if (mercaderiaRepository.existsBySkuAndIdMercaderiaNot(request.sku(), id)) {
            throw new ReglaNegocioException("Ya existe otra mercadería con el SKU: " + request.sku());
        }

        validarDatosDelTipo(request);

        /* Si el proveedor no cambia, se conserva (aunque se haya dado de baja después) */
        Proveedor proveedor = mercaderia.getProveedor().getIdProveedor().equals(request.idProveedor())
                ? mercaderia.getProveedor()
                : obtenerProveedorActivo(request.idProveedor());

        copiarDatosComunes(mercaderia, request, proveedor);
        copiarDatosDelTipo(mercaderia, request);

        return aResponse(mercaderiaRepository.save(mercaderia));
    }

    /* ==============================
     *       D - ELIMINAR LÓGICO
     * ============================== */

    /* DESACTIVAR: la fila se queda, solo se marca como inactiva */
    @Transactional
    public void desactivar(Integer id, BajaMercaderiaRequest request) {
        Mercaderia mercaderia = obtenerPorId(id);

        if (!mercaderia.getActivo()) {
            throw new ReglaNegocioException("La mercadería ya está desactivada.");
        }

        /* Si el motivo es "Otro", el comentario es lo único que explica la baja, así que es obligatorio */
        if (request.motivo() == MotivoBaja.OTRO && (request.comentario() == null || request.comentario().isBlank())) {
            throw new ReglaNegocioException("Cuando el motivo es OTRO, debe indicar un comentario.");
        }

        mercaderia.setActivo(false);
        mercaderiaRepository.save(mercaderia);

        /* Misma transacción: si la auditoría falla, la baja también se revierte */
        auditoriaService.registrar(AccionAuditoria.INACTIVACION, AuditoriaService.ENTIDAD_MERCADERIA,
                mercaderia.getSku(), AuditoriaService.componerMotivo(request.motivo(), request.comentario()));
    }

    /* REACTIVAR: vuelve a marcar como activa y deja constancia en la auditoría (el motivo de la tabla es obligatorio) */
    @Transactional
    public void reactivar(Integer id, String comentario) {
        Mercaderia mercaderia = obtenerPorId(id);

        if (mercaderia.getActivo()) {
            throw new ReglaNegocioException("La mercadería ya está activa.");
        }

        mercaderia.setActivo(true);
        mercaderiaRepository.save(mercaderia);

        String motivo = (comentario == null || comentario.isBlank())
                ? "Reactivación de mercadería"
                : "Reactivación de mercadería: " + comentario.trim();
        auditoriaService.registrar(AccionAuditoria.REACTIVACION, AuditoriaService.ENTIDAD_MERCADERIA,
                mercaderia.getSku(), motivo);
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* Busca por id (con su proveedor) o lanza el 404 */
    private Mercaderia obtenerPorId(Integer id) {
        return mercaderiaRepository.buscarPorIdConProveedor(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ninguna mercadería con el id: " + id));
    }

    /* El proveedor debe existir y no estar dado de baja */
    private Proveedor obtenerProveedorActivo(Integer idProveedor) {
        Proveedor proveedor = proveedorRepository.findById(idProveedor).orElseThrow(
                () -> new ReglaNegocioException("No existe un proveedor con el id: " + idProveedor));
        if (!proveedor.getActivo()) {
            throw new ReglaNegocioException("El proveedor indicado está dado de baja.");
        }
        return proveedor;
    }

    /* El tipo de repuesto debe existir */
    private TipoRepuesto obtenerTipoRepuesto(Integer idTipoRepuesto) {
        return tipoRepuestoRepository.findById(idTipoRepuesto).orElseThrow(
                () -> new ReglaNegocioException("No existe un tipo de repuesto con el id: " + idTipoRepuesto));
    }

    /* Revisa que vengan los datos obligatorios del tipo elegido */
    private void validarDatosDelTipo(MercaderiaRequest r) {
        switch (r.tipo()) {
            case SCOOTER -> {
                if (r.autonomiaKm() == null || r.potenciaWatts() == null
                        || r.mesesGarantia() == null || esVacio(r.color())) {
                    throw new ReglaNegocioException("Un scooter requiere: autonomiaKm, potenciaWatts, color y mesesGarantia.");
                }
            }
            case REPUESTO -> {
                if (r.idTipoRepuesto() == null || esVacio(r.modelosCompatibles())) {
                    throw new ReglaNegocioException("Un repuesto requiere: idTipoRepuesto y modelosCompatibles.");
                }
            }
            case ACCESORIO -> {
                /* La talla y el color son opcionales */
            }
        }
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /* Crea el objeto vacío de la clase hija que corresponde al tipo */
    private Mercaderia crearVacia(TipoMercaderia tipo) {
        return switch (tipo) {
            case SCOOTER -> new Scooter();
            case REPUESTO -> new Repuesto();
            case ACCESORIO -> new Accesorio();
        };
    }

    /* Copia los datos que tienen en común los tres tipos (el stock NO se copia) */
    private void copiarDatosComunes(Mercaderia m, MercaderiaRequest r, Proveedor proveedor) {
        m.setSku(r.sku());
        m.setDescripcion(r.descripcion());
        m.setStockMinimo(r.stockMinimo());
        m.setPrecioCosto(r.precioCosto());
        m.setPrecioVenta(r.precioVenta());
        m.setProveedor(proveedor);
    }

    /* Copia los datos propios de cada tipo */
    private void copiarDatosDelTipo(Mercaderia m, MercaderiaRequest r) {
        if (m instanceof Scooter s) {
            s.setAutonomiaKm(r.autonomiaKm());
            s.setPotenciaWatts(r.potenciaWatts());
            s.setColor(r.color());
            s.setMesesGarantia(r.mesesGarantia());
        } else if (m instanceof Repuesto rep) {
            rep.setTipoRepuesto(obtenerTipoRepuesto(r.idTipoRepuesto()));
            rep.setModelosCompatibles(r.modelosCompatibles());
        } else if (m instanceof Accesorio a) {
            a.setTalla(r.talla());
            a.setColor(r.color());
        }
    }

    /* Devuelve el tipo de un producto ya guardado, según su clase */
    private TipoMercaderia tipoDe(Mercaderia m) {
        if (m instanceof Scooter) return TipoMercaderia.SCOOTER;
        if (m instanceof Repuesto) return TipoMercaderia.REPUESTO;
        if (m instanceof Accesorio) return TipoMercaderia.ACCESORIO;
        throw new IllegalStateException("Tipo de mercadería desconocido: " + m.getClass().getSimpleName());
    }

    /* MÉTODO PRIVADO: convierte Entidad -> Response, según el tipo real del producto */
    /* MÉTODO PRIVADO: convierte Entidad -> Response, según el tipo real del producto */
    private MercaderiaResponse aResponse(Mercaderia m) {
        return construirResponse(m, null);
    }

    /* Igual que antes, pero con los datos de la baja (que solo se llenan al buscar por id) */
    private MercaderiaResponse construirResponse(Mercaderia m, MercaderiaResponse.InfoBaja baja) {
        TipoMercaderia tipo;
        Integer autonomiaKm = null;
        Integer potenciaWatts = null;
        Integer mesesGarantia = null;
        String color = null;
        Integer idTipoRepuesto = null;
        String nombreTipoRepuesto = null;
        String modelosCompatibles = null;
        String talla = null;

        if (m instanceof Scooter s) {
            tipo = TipoMercaderia.SCOOTER;
            autonomiaKm = s.getAutonomiaKm();
            potenciaWatts = s.getPotenciaWatts();
            mesesGarantia = s.getMesesGarantia();
            color = s.getColor();
        } else if (m instanceof Repuesto r) {
            tipo = TipoMercaderia.REPUESTO;
            idTipoRepuesto = r.getTipoRepuesto().getIdTipoRepuesto();
            nombreTipoRepuesto = r.getTipoRepuesto().getNombre();
            modelosCompatibles = r.getModelosCompatibles();
        } else if (m instanceof Accesorio a) {
            tipo = TipoMercaderia.ACCESORIO;
            talla = a.getTalla();
            color = a.getColor();
        } else {
            throw new IllegalStateException("Tipo de mercadería desconocido: " + m.getClass().getSimpleName());
        }

        return new MercaderiaResponse(
                m.getIdMercaderia(),
                tipo,
                m.getSku(),
                m.getDescripcion(),
                m.getStock(),
                m.getStockMinimo(),
                m.getPrecioCosto(),
                m.getPrecioVenta(),
                m.getFechaIngreso(),
                m.getActivo(),
                m.getProveedor().getIdProveedor(),
                m.getProveedor().getRazonSocial(),
                autonomiaKm,
                potenciaWatts,
                mesesGarantia,
                color,
                idTipoRepuesto,
                nombreTipoRepuesto,
                modelosCompatibles,
                talla,
                baja);
    }
}