package com.voltiopedal.apiinventario.auditoria;

import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuditoriaService {
    private final RegistroAuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(RegistroAuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /* MANDATORY: la auditoría se guarda en la MISMA transacción que la acción. Si la acción falla, no queda auditoría */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(Integer idUsuario, AccionAuditoria accion, String entidad, String idRegistro, String motivo) {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setFechaHora(LocalDateTime.now());
        registro.setIdUsuario(idUsuario);
        registro.setAccion(accion);
        registro.setEntidadAfectada(entidad);
        registro.setIdRegistro(idRegistro.length() > 50 ? idRegistro.substring(0, 50) : idRegistro);
        registro.setMotivo(motivo.trim());
        auditoriaRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listar(String entidad, AccionAuditoria accion) {
        List<RegistroAuditoria> registros = auditoriaRepository.buscar(entidad, accion);

        Set<Integer> ids = registros.stream().map(r -> r.getIdUsuario()).collect(Collectors.toSet());
        Map<Integer, String> nombres = usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(u -> u.getIdUsuario(), u -> u.getNombres() + " " + u.getApellidos()));

        return registros.stream()
                .map(r -> new AuditoriaResponse(
                        r.getIdRegistroAuditoria(), r.getFechaHora(),
                        nombres.getOrDefault(r.getIdUsuario(), "Usuario #" + r.getIdUsuario()),
                        r.getAccion(), r.getEntidadAfectada(), r.getIdRegistro(), r.getMotivo()))
                .toList();
    }
}