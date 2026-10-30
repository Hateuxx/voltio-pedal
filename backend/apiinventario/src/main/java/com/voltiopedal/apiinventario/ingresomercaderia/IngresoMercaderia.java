package com.voltiopedal.apiinventario.ingresomercaderia;

import com.voltiopedal.apiinventario.proveedor.Proveedor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingreso_mercaderia")
@Getter @Setter @NoArgsConstructor
public class IngresoMercaderia {

    /* INT UNSIGNED -> Integer */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso")
    private Integer idIngreso;

    /* Relación con proveedor: muchos ingresos pertenecen a un proveedor */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    /* VARCHAR(20) NOT NULL -> String */
    @Column(name = "numero_guia", nullable = false, length = 20)
    private String numeroGuia;

    /* DATETIME NOT NULL -> LocalDateTime */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /* Usuario que recibió la mercadería (solo se guarda su id) */
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    /* VARCHAR(255) NULL -> String */
    @Column(name = "observacion", length = 255)
    private String observacion;

    /* TINYINT(1) NOT NULL DEFAULT 1 -> Boolean */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}