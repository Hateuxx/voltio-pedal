package com.voltiopedal.apiinventario.auditoria;

import com.voltiopedal.apiinventario.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_auditoria")
@Getter @Setter @NoArgsConstructor
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro_auditoria")
    private Integer idRegistroAuditoria;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 50)
    private AccionAuditoria accion;

    @Column(name = "entidad_afectada", nullable = false, length = 100)
    private String entidadAfectada;

    @Column(name = "id_registro", nullable = false, length = 50)
    private String codigoRegistro;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;
}