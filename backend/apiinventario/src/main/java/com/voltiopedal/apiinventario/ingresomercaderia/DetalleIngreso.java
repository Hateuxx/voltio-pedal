package com.voltiopedal.apiinventario.ingresomercaderia;

import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_ingreso")
@Getter @Setter @NoArgsConstructor
public class DetalleIngreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_ingreso")
    private Integer idDetalleIngreso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ingreso", nullable = false)
    private IngresoMercaderia ingreso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mercaderia", nullable = false)
    private Mercaderia mercaderia;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoUnitario;
}