package com.voltiopedal.apiinventario.auditoria;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/* Solo se inserta: nunca se edita ni se borra */
@Entity
@Table(name = "registro_auditoria")
@Getter @Setter @NoArgsConstructor
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro_auditoria")
    private Integer idRegistroAuditoria;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 50)
    private AccionAuditoria accion;

    @Column(name = "entidad_afectada", nullable = false, length = 100)
    private String entidadAfectada;

    /* Código legible (RUC, SKU, nombre): SIN FK para que sobreviva a la eliminación física */
    @Column(name = "id_registro", nullable = false, length = 50)
    private String idRegistro;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;
}