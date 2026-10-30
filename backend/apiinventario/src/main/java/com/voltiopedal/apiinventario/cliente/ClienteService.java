package com.voltiopedal.apiinventario.cliente;

import org.springframework.stereotype.Service;

import com.voltiopedal.apiinventario.common.RecursoNoEncontradoException;
import com.voltiopedal.apiinventario.common.ReglaNegocioException;

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

    /* Registrar */
    @Transactional
    public ClienteResponse registrar(ClienteRequest request) {

        TipoDocumentoCliente tipo = request.tipoDocumento();
        String numero = request.numeroDocumento().trim();
        String nombres = request.nombres().trim().replaceAll("\\s+", " ");
        String apellidos = limpiarOpcional(request.apellidos());
        String telefono = limpiarOpcional(request.telefono());

        /* A. DNI: EXACTAMENTE 8 DIGITOS */
        if (tipo == TipoDocumentoCliente.DNI && !numero.matches("^[0-9]{8}$")) {
            throw new ReglaNegocioException("El DNI debe tener exactamente 8 números.");
        }

        /* B. RUC: 11 DIGITOS */
        if (tipo == TipoDocumentoCliente.RUC && !numero.matches("^[0-9]{11}$")) {
            throw new ReglaNegocioException("El RUC debe tener exactamente 11 números.");
        }

        /* C. Si no es RUC, los apellidos son obligatorios */
        if (tipo != TipoDocumentoCliente.RUC && (apellidos == null || apellidos.isBlank())) {
            throw new ReglaNegocioException("Los apellidos son obligatorios, salvo cuando el documento es RUC");
        }

        /*Revisar duplicado */
        if (clienteRepository.existsByTipoDocumentoAndNumeroDocumento(tipo, numero)){
            throw new ReglaNegocioException("Ya existe un cliente con el documento " + tipo + " " + numero + ".");
        }

        /*Crear la entidad con los datos limpios, guardarla y devolver el Response */
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento(tipo);
        cliente.setNumeroDocumento(numero);
        cliente.setNombres(nombres);
        cliente.setApellidos(apellidos);
        cliente.setTelefono(telefono);
        Cliente guardado = clienteRepository.save(cliente);

        return aResponse(guardado);
    }

    /* ==============================
     *          R - CONSULTAR
     * ============================== */

    /* Listar */
    public List<ClienteResponse> listar() {
        return clienteRepository.findAllByOrderByNombresAsc().stream().map(this::aResponse).toList();
    }

    /* Buscar por Documento */
    public ClienteResponse buscarPorDocumento(TipoDocumentoCliente tipoDocumento, String numeroDocumento) {
        Cliente cliente = clienteRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento)
                .orElseThrow(
                        () -> new RecursoNoEncontradoException(
                                "No se encontró ningún cliente con el documento: " + numeroDocumento));
        return aResponse(cliente);
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* Limpia un texto opcional: quita espacios y devuelve null si queda vacío */
    private String limpiarOpcional(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim().replaceAll("\\s+", " ");

        return limpio.isEmpty() ? null : limpio;
    }

    private ClienteResponse aResponse(Cliente c) {
        return new ClienteResponse(
                c.getIdCliente(),
                c.getTipoDocumento(),
                c.getNumeroDocumento(),
                c.getNombres(),
                c.getApellidos(),
                c.getTelefono());
    }
}