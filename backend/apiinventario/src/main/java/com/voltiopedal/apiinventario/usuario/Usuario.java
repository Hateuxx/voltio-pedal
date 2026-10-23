package com.voltiopedal.apiinventario.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity 
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor
public class Usuario {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    /* JPA que guarde el texto del Enum, no su posición numérica */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "contrasena", nullable = false, length = 60)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "pregunta_seguridad", nullable = false, length = 150)
    private String preguntaSeguridad;

    @Column(name = "respuesta_seguridad", nullable = false, length = 60)
    private String respuestaSeguridad;

    /* TINYINT UNSIGNED -> Byte inicializado en 0 por el DEFAULT de la base de datos */
    @Column(name = "intentos_fallidos", nullable = false)
    private Byte intentosFallidos = (byte) 0;

    /* DATETIME NULL -> LocalDateTime. No lleva nullable = false porque puede estar vacio */
    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    /* TINYINT(1) -> Boolean inicializado en true */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
