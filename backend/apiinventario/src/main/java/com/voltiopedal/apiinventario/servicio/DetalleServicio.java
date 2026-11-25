package com.voltiopedal.apiinventario.servicio;

import com.voltiopedal.apiinventario.mercaderia.Repuesto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* RF17: repuesto consumido en un servicio técnico */
@Entity
@Table(name = "detalle_servicio")
@Getter @Setter @NoArgsConstructor
public class DetalleServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_servicio")
    private Integer idDetalleServicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio_tecnico", nullable = false)
    private ServicioTecnico servicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_repuesto", nullable = false)
    private Repuesto repuesto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
}
