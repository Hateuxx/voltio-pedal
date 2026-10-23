package com.voltiopedal.apiinventario.proveedor;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {
    private final ProveedorRepository proveedorRepository;

    /* Insertar dependencia recomendada por constructor */
    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

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

    /*
     * REGISTRAR recibe el Request, crea la Entidad, guarda y devuelve el Response
     */
    /*
     * @Transactional garantiza que o se guarda todo perfecto, o se revierte
     * (rollback)
     */
    @Transactional
    public ProveedorResponse registrar(ProveedorRequest request) {
        /* 1ero Buscar siel RUC ya existe y guardamos el resultado en la caja Optional */
        Optional<Proveedor> existente = proveedorRepository.findByRuc(request.ruc());

        if(existente.isPresent()) {             /* ¿hay algo en la caja? */
            if (existente.get().getActivo()) {  /* Sí, y está ACTIVO */
                throw new ReglaNegocioException("Ya existe un proveedor registrado con el RUC: " + request.ruc());
            }
            /* sí, pero está DADO DE BAJA */
            throw new ReglaNegocioException("El proveedor con RUC " + request.ruc() + "existé, pero se encuentra DADO DE BAJA. Diríjase a la sección de inactivos para reactivarlo.");
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
                p.getActivo()
        );
    }
}
