package com.voltiopedal.apiinventario.proveedor;

import com.voltiopedal.apiinventario.auditoria.AccionAuditoria;
import com.voltiopedal.apiinventario.auditoria.AuditoriaService;
import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {
    private final ProveedorRepository proveedorRepository;
    private final AuditoriaService auditoriaService;

    /* Insertar dependencia recomendada por constructor */
    public ProveedorService(ProveedorRepository proveedorRepository, AuditoriaService auditoriaService) {
        this.proveedorRepository = proveedorRepository;
        this.auditoriaService = auditoriaService;
    }

    /*
     * REGISTRAR recibe el Request, crea la Entidad, guarda y devuelve el Response
     */

    /*
     * @Transactional garantiza que o se guarda todo perfecto, o se revierte
     * (rollback)
     */

    /*
     * ==============================
     * C - CREAR
     * ==============================
     */

    @Transactional
    public ProveedorResponse registrar(ProveedorRequest request) {
        /*
         * 1ero Buscar si el RUC ya existe y guardamos el resultado en la caja Optional
         */
        Optional<Proveedor> existente = proveedorRepository.findByRuc(request.ruc());

        if (existente.isPresent()) { /* ¿hay algo en la caja? */
            if (existente.get().getActivo()) { /* Sí, y está ACTIVO */
                throw new ReglaNegocioException("Ya existe un proveedor registrado con el RUC: " + request.ruc());
            }
            /* sí, pero está DADO DE BAJA */
            throw new ReglaNegocioException("El proveedor con RUC " + request.ruc() +
                    " existe, pero se encuentra DADO DE BAJA. Diríjase a la sección de inactivos para reactivarlo.");
        }

        /* Mapeo manual (de Request a Entidad) */
        Proveedor proveedor = new Proveedor();
        proveedor.setRuc(request.ruc());
        proveedor.setRazonSocial(request.razonSocial());
        proveedor.setContacto(request.contacto());
        proveedor.setTelefono(request.telefono());
        proveedor.setRubro(request.rubro());
        proveedor.setFrecuenciaEntrega(request.frecuenciaEntrega());

        /*
         * Ya no se necesita forzar el ID a null o activo a true porque al hacer
         * "new Proveedor()"
         */
        /*
         * nacen con esos valores por defecto, y el Request no tiene forma de alterarlos
         */

        Proveedor guardado = proveedorRepository.save(proveedor);
        return aResponse(guardado);
    }

    /*
     * ==============================
     * R - CONSULTAR
     * ==============================
     */

    /*
     * Listar uso un Stream para convertir la lista de Entidades a lista de
     * Responses
     */
    public List<ProveedorResponse> listarActivos() {
        return proveedorRepository.findByActivoTrue().stream().map(this::aResponse).toList();
    }

    /* BUSCAR */
    public ProveedorResponse buscarPorRuc(String ruc) {
        Proveedor proveedor = proveedorRepository.findByRuc(ruc).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún proveedor con el RUC: " + ruc));
        return aResponse(proveedor);
    }

    /* BUSCAR POR ID */
    public ProveedorResponse buscarPorId(Integer id) {
        return aResponse(obtenerPorId(id));
    }

    /*
     * ==============================
     * U - ACTUALIZAR
     * ==============================
     */

    /* ACTUALIZAR (U): no toca ni el id ni el campo activo */
    @Transactional
    public ProveedorResponse actualizar(Integer id, ProveedorRequest request) {
        Proveedor proveedor = obtenerPorId(id);

        if (proveedorRepository.existsByRucAndIdProveedorNot(request.ruc(), id)) {
            throw new ReglaNegocioException("Ya existe otro proveedor con el RUC: " + request.ruc());
        }

        proveedor.setRuc(request.ruc());
        proveedor.setRazonSocial(request.razonSocial());
        proveedor.setContacto(request.contacto());
        proveedor.setTelefono(request.telefono());
        proveedor.setRubro(request.rubro());
        proveedor.setFrecuenciaEntrega(request.frecuenciaEntrega());

        return aResponse(proveedorRepository.save(proveedor));
    }

    /*
     * ==============================
     * D - ELIMINAR LÓGICO
     * ==============================
     */

    /* ELIMINACIÓN LÓGICA (D): la fila se queda, solo se marca como inactivo */
    @Transactional
    public void desactivar(Integer id, String motivo, Integer idUsuario) {
        Proveedor proveedor = obtenerPorId(id);
        if (!proveedor.getActivo()) {
            throw new ReglaNegocioException("El proveedor ya está dado de baja.");
        }
        proveedor.setActivo(false);
        auditoriaService.registrar(idUsuario, AccionAuditoria.INACTIVACION, "PROVEEDOR", proveedor.getRuc(), motivo);
    }

    /*
     * ==============================
     * D - ELIMINAR FÍSICO
     * ==============================
     */

    /* ELIMINACIÓN FÍSICA (D): borra la fila de la base de datos */
    @Transactional
    public void eliminarFisico(Integer id, String motivo, Integer idUsuario) {
        Proveedor proveedor = obtenerPorId(id);
        String ruc = proveedor.getRuc(); /* se guarda ANTES de borrar */
        try {
            proveedorRepository.delete(proveedor);
            proveedorRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el proveedor porque tiene mercadería o ingresos asociados. Use 'Dar de baja'.");
        }
        auditoriaService.registrar(idUsuario, AccionAuditoria.ELIMINACION_FISICA, "PROVEEDOR", ruc, motivo);
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponse> listarInactivos() {
        return proveedorRepository.findByActivoFalse().stream().map(this::aResponse)/*
                                                                                     * 0 proveedor ->
                                                                                     * aResponse(proveedor)
                                                                                     */.toList();
    }

    @Transactional
    public void reactivar(Integer id, String motivo, Integer idUsuario) {
        Proveedor proveedor = obtenerPorId(id);
        if (proveedor.getActivo()) {
            throw new ReglaNegocioException("El proveedor ya está activo.");
        }
        proveedor.setActivo(true);
        auditoriaService.registrar(idUsuario, AccionAuditoria.REACTIVACION, "PROVEEDOR", proveedor.getRuc(), motivo);
    }

    /*
     * ==============================
     * MÉTODOS AUXILIARES
     * ==============================
     */

    /* MÉTODO PRIVADO: busca por id o lanza el 404 */
    private Proveedor obtenerPorId(Integer id) {
        return proveedorRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún proveedor con el id: " + id));
    }

    /* MÉTODO PRIVADO DRY: Convierte Entidad ->Response */
    private ProveedorResponse aResponse(Proveedor p) {
        return new ProveedorResponse(
                p.getIdProveedor(),
                p.getRuc(),
                p.getRazonSocial(),
                p.getContacto(),
                p.getTelefono(),
                p.getRubro(),
                p.getFrecuenciaEntrega(),
                p.getActivo());
    }
}
