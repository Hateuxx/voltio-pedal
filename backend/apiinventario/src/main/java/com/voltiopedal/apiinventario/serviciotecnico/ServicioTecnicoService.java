package com.voltiopedal.apiinventario.serviciotecnico;

import com.voltiopedal.apiinventario.cliente.Cliente;
import com.voltiopedal.apiinventario.cliente.ClienteRepository;
import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import com.voltiopedal.apiinventario.mercaderia.MercaderiaRepository;
import com.voltiopedal.apiinventario.mercaderia.Scooter;
import com.voltiopedal.apiinventario.usuario.Rol;
import com.voltiopedal.apiinventario.usuario.Usuario;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioTecnicoService {
    private final ServicioTecnicoRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final MercaderiaRepository mercaderiaRepository;
    private final UsuarioRepository usuarioRepository;

    /* Insertar dependencias por constructor */
    public ServicioTecnicoService(ServicioTecnicoRepository servicioRepository,
                                  ClienteRepository clienteRepository,
                                  MercaderiaRepository mercaderiaRepository,
                                  UsuarioRepository usuarioRepository) {
        this.servicioRepository = servicioRepository;
        this.clienteRepository = clienteRepository;
        this.mercaderiaRepository = mercaderiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /* ==============================
     *          C - CREAR
     * ============================== */

    /* REGISTRAR: ingreso de un equipo al taller */
    @Transactional
    public ServicioTecnicoResponse registrar(ServicioTecnicoRequest request) {
        /* 1. Las referencias deben existir */
        Cliente cliente = clienteRepository.findById(request.idCliente()).orElseThrow(
                () -> new ReglaNegocioException("No existe un cliente con el id: " + request.idCliente()));
        Scooter scooter = obtenerScooter(request.idScooter());
        Usuario tecnico = request.idTecnico() != null ? obtenerTecnicoActivo(request.idTecnico()) : null;

        /* 2. El mismo equipo no puede tener dos servicios en curso */
        if (servicioRepository.existsByNumeroSerieAndEstadoNot(request.numeroSerie(), EstadoServicio.ENTREGADO)) {
            throw new ReglaNegocioException("El equipo con número de serie " + request.numeroSerie()
                    + " ya tiene un servicio técnico en curso.");
        }

        /* 3. Se arma el servicio (estado INGRESADO y fecha de ingreso actual vienen por defecto) */
        ServicioTecnico servicio = new ServicioTecnico();
        servicio.setCliente(cliente);
        servicio.setScooter(scooter);
        servicio.setTecnico(tecnico);
        servicio.setNumeroSerie(request.numeroSerie());
        servicio.setFallaReportada(request.fallaReportada());
        servicio.setFechaCompra(request.fechaCompra());

        /* 4. Garantía: meses enviados o, si no, los del scooter; se calcula si está cubierto */
        Integer meses = request.mesesGarantia() != null ? request.mesesGarantia() : scooter.getMesesGarantia();
        servicio.setMesesGarantia(meses);
        servicio.setCubiertoPorGarantia(calcularGarantia(request.fechaCompra(), meses, servicio.getFechaIngreso()));

        return aResponse(servicioRepository.save(servicio));
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* Lista todos los servicios, del más reciente al más antiguo */
    public List<ServicioTecnicoResponse> listar() {
        return servicioRepository.listarConRelaciones().stream().map(this::aResponse).toList();
    }

    /* Busca un servicio por su id */
    public ServicioTecnicoResponse buscarPorId(Integer id) {
        return aResponse(obtenerPorId(id));
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    /* ACTUALIZAR: estado, diagnóstico y técnico */
    @Transactional
    public ServicioTecnicoResponse actualizar(Integer id, ServicioTecnicoActualizacionRequest request) {
        ServicioTecnico servicio = obtenerPorId(id);

        /* Un servicio entregado queda cerrado */
        if (servicio.getEstado() == EstadoServicio.ENTREGADO) {
            throw new ReglaNegocioException("El servicio ya fue entregado y no se puede modificar.");
        }

        /* Lo que no se envía se conserva */
        if (request.idTecnico() != null) {
            servicio.setTecnico(obtenerTecnicoActivo(request.idTecnico()));
        }
        if (request.diagnostico() != null) {
            servicio.setDiagnostico(request.diagnostico());
        }

        /* Para entregar, debe haber un diagnóstico y se registra la fecha de entrega */
        if (request.estado() == EstadoServicio.ENTREGADO) {
            if (servicio.getDiagnostico() == null || servicio.getDiagnostico().isBlank()) {
                throw new ReglaNegocioException("Para marcar el servicio como ENTREGADO debe registrar el diagnóstico.");
            }
            servicio.setFechaEntrega(LocalDateTime.now());
        }
        servicio.setEstado(request.estado());

        return aResponse(servicioRepository.save(servicio));
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* Busca por id (con sus relaciones) o lanza el 404 */
    private ServicioTecnico obtenerPorId(Integer id) {
        return servicioRepository.buscarPorIdConRelaciones(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún servicio técnico con el id: " + id));
    }

    /* El id debe corresponder a una mercadería de tipo scooter */
    private Scooter obtenerScooter(Integer idScooter) {
        Mercaderia mercaderia = mercaderiaRepository.findById(idScooter).orElseThrow(
                () -> new ReglaNegocioException("No existe una mercadería con el id: " + idScooter));
        if (mercaderia instanceof Scooter scooter) {
            return scooter;
        }
        throw new ReglaNegocioException("La mercadería con id " + idScooter + " no es un scooter.");
    }

    /* El técnico debe existir, estar activo y tener el rol TECNICO */
    private Usuario obtenerTecnicoActivo(Integer idTecnico) {
        Usuario usuario = usuarioRepository.findById(idTecnico).orElseThrow(
                () -> new ReglaNegocioException("No existe un usuario con el id: " + idTecnico));
        if (usuario.getRol() != Rol.TECNICO) {
            throw new ReglaNegocioException("El usuario indicado no tiene el rol de técnico.");
        }
        if (!usuario.getActivo()) {
            throw new ReglaNegocioException("El técnico indicado está desactivado.");
        }
        return usuario;
    }

    /* Está en garantía si hay fecha de compra y meses, y el ingreso cae dentro del plazo */
    private boolean calcularGarantia(LocalDate fechaCompra, Integer meses, LocalDateTime fechaIngreso) {
        if (fechaCompra == null || meses == null) {
            return false;
        }
        return !fechaIngreso.toLocalDate().isAfter(fechaCompra.plusMonths(meses));
    }

    /* MÉTODO PRIVADO: convierte Entidad -> Response */
    private ServicioTecnicoResponse aResponse(ServicioTecnico s) {
        Cliente c = s.getCliente();
        Scooter sc = s.getScooter();
        /* El técnico puede ser null (todavía sin asignar) */
        Usuario t = s.getTecnico();

        return new ServicioTecnicoResponse(
                s.getIdServicioTecnico(),
                s.getFechaIngreso(),
                s.getFechaEntrega(),
                s.getNumeroSerie(),
                s.getFallaReportada(),
                s.getDiagnostico(),
                s.getEstado(),
                s.getFechaCompra(),
                s.getMesesGarantia(),
                s.getCubiertoPorGarantia(),
                c.getIdCliente(),
                c.getNumeroDocumento(),
                c.getNombres(),
                c.getApellidos(),
                sc.getIdMercaderia(),
                sc.getSku(),
                sc.getDescripcion(),
                t != null ? t.getIdUsuario() : null,
                t != null ? t.getNombres() : null,
                t != null ? t.getApellidos() : null);
    }
}
