package com.voltiopedal.apiinventario.conteo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conteo_inventario")
@Getter @Setter @NoArgsConstructor
public class ConteoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conteo")
    private Integer idConteo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoConteo estado = EstadoConteo.ABIERTO;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "observacion", length = 255)
    private String observacion;

    @OneToMany(mappedBy = "conteo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idDetalleConteo ASC")
    private List<DetalleConteo> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleConteo detalle) {
        detalle.setConteo(this);
        detalles.add(detalle);
    }
}