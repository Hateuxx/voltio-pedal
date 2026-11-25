package com.voltiopedal.apiinventario.auditoria;

import com.voltiopedal.apiinventario.common.ReglaNegocioException;
import com.voltiopedal.apiinventario.usuario.Usuario;
import com.voltiopedal.apiinventario.usuario.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/* Escribe en registro_auditoria. El usuario se toma del token JWT de la petición en curso */
@Service
public class AuditoriaService {
    /* Nombre de entidad que se guarda para la mercadería */
    public static final String ENTIDAD_MERCADERIA = "MERCADERIA";

    private final RegistroAuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(RegistroAuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /* Guarda un registro. Se une a la transacción del servicio que lo llama: si algo falla, se revierte todo junto */
    @Transactional
    public void registrar(AccionAuditoria accion, String entidad, String codigo, String motivo) {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setUsuario(usuarioActual());
        registro.setAccion(accion);
        registro.setEntidadAfectada(entidad);
        registro.setCodigoRegistro(codigo);
        registro.setMotivo(motivo);
        auditoriaRepository.save(registro);
    }

    /* ==============================
     *   FORMATO DEL MOTIVO GUARDADO
     * ============================== */

    /* "DEFECTO" o "DEFECTO: se rompió el freno". La columna motivo es un solo texto, por eso se guarda así */
    public static String componerMotivo(MotivoBaja motivo, String comentario) {
        String limpio = comentario == null ? "" : comentario.trim();
        return limpio.isEmpty() ? motivo.name() : motivo.name() + ": " + limpio;
    }

    /* Saca la categoría del texto guardado; si no se reconoce (registro antiguo o manual), cuenta como OTRO */
    public static MotivoBaja categoriaDe(String motivoGuardado) {
        if (motivoGuardado == null) return MotivoBaja.OTRO;
        int corte = motivoGuardado.indexOf(':');
        String codigo = (corte >= 0 ? motivoGuardado.substring(0, corte) : motivoGuardado).trim();
        try {
            return MotivoBaja.valueOf(codigo);
        } catch (IllegalArgumentException e) {
            return MotivoBaja.OTRO;
        }
    }

    /* Saca solo el comentario del texto guardado ("" si no hay) */
    public static String comentarioDe(String motivoGuardado) {
        if (motivoGuardado == null) return "";
        int corte = motivoGuardado.indexOf(':');
        return corte >= 0 ? motivoGuardado.substring(corte + 1).trim() : "";
    }

    /* ==============================
     *       MÉTODOS AUXILIARES
     * ============================== */

    /* El "sub" del token es el id del usuario (ver ServicioToken) */
    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ReglaNegocioException("No se pudo identificar al usuario que realiza la operación.");
        }
        try {
            Integer idUsuario = Integer.valueOf(auth.getName());
            return usuarioRepository.findById(idUsuario).orElseThrow(
                    () -> new ReglaNegocioException("El usuario que realiza la operación ya no existe."));
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("No se pudo identificar al usuario que realiza la operación.");
        }
    }

    /* Última baja registrada de un código (por ejemplo, un SKU); vacío si no dejó registro */
    @Transactional(readOnly = true)
    public Optional<RegistroAuditoria> ultimaBaja(String entidad, String codigo) {
        List<RegistroAuditoria> bajas =
                auditoriaRepository.listarPorRegistroYAccion(entidad, codigo, AccionAuditoria.INACTIVACION);
        return bajas.isEmpty() ? Optional.empty() : Optional.of(bajas.get(0));
    }
}