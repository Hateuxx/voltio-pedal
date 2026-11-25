package com.voltiopedal.apiinventario.cliente;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /* ==============================
     *          C - CREAR
     * ============================== */

    @Transactional
    public ClienteResponse registrar(ClienteRequest request) {
        Cliente cliente = new Cliente();
        aplicarDatos(cliente, request, null);
        return aResponse(clienteRepository.save(cliente));
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAllByOrderByNombresAsc().stream().map(c -> aResponse(c)).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Integer id) {
        return aResponse(obtenerPorId(id));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorDocumento(TipoDocumentoCliente tipoDocumento, String numeroDocumento) {
        Cliente cliente = clienteRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró ningún cliente con el documento: " + numeroDocumento));
        return aResponse(cliente);
    }

    /* ==============================
     *          U - ACTUALIZAR
     * ============================== */

    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = obtenerPorId(id);
        aplicarDatos(cliente, request, id);
        return aResponse(cliente);
    }

    /* ==============================
     *       D - ELIMINAR FÍSICO
     * ============================== */

    @Transactional
    public void eliminar(Integer id) {
        Cliente cliente = obtenerPorId(id);
        try {
            clienteRepository.delete(cliente);
            clienteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException("No se puede eliminar el cliente porque tiene ventas o servicios técnicos registrados.");
        }
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* Valida y copia los datos (sirve para crear y editar). idActual = null al crear */
    private void aplicarDatos(Cliente cliente, ClienteRequest request, Integer idActual) {
        TipoDocumentoCliente tipo = request.tipoDocumento();
        String numero = request.numeroDocumento().trim();
        String nombres = request.nombres().trim().replaceAll("\\s+", " ");
        String apellidos = limpiarOpcional(request.apellidos());
        String telefono = limpiarOpcional(request.telefono());

        if (tipo == TipoDocumentoCliente.DNI && !numero.matches("^[0-9]{8}$")) {
            throw new ReglaNegocioException("El DNI debe tener exactamente 8 números.");
        }
        if (tipo == TipoDocumentoCliente.RUC && !numero.matches("^[0-9]{11}$")) {
            throw new ReglaNegocioException("El RUC debe tener exactamente 11 números.");
        }
        if (tipo != TipoDocumentoCliente.RUC && apellidos == null) {
            throw new ReglaNegocioException("Los apellidos son obligatorios, salvo cuando el documento es RUC.");
        }

        clienteRepository.findByTipoDocumentoAndNumeroDocumento(tipo, numero).ifPresent(otro -> {
            if (idActual == null || !otro.getIdCliente().equals(idActual)) {
                throw new ReglaNegocioException("Ya existe un cliente con el documento " + tipo + " " + numero + ".");
            }
        });

        cliente.setTipoDocumento(tipo);
        cliente.setNumeroDocumento(numero);
        cliente.setNombres(nombres);
        cliente.setApellidos(tipo == TipoDocumentoCliente.RUC ? null : apellidos);
        cliente.setTelefono(telefono);
    }

    private Cliente obtenerPorId(Integer id) {
        return clienteRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró ningún cliente con el id: " + id));
    }

    /* Quita espacios sobrantes y devuelve null si queda vacío */
    private String limpiarOpcional(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim().replaceAll("\\s+", " ");
        return limpio.isEmpty() ? null : limpio;
    }

    private ClienteResponse aResponse(Cliente c) {
        return new ClienteResponse(c.getIdCliente(), c.getTipoDocumento(), c.getNumeroDocumento(),
                c.getNombres(), c.getApellidos(), c.getTelefono());
    }
}
