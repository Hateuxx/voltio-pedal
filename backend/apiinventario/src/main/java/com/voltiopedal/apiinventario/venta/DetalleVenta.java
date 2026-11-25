package com.voltiopedal.apiinventario.venta;

import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
@Getter @Setter @NoArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_venta")
    private Integer idDetalleVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mercaderia", nullable = false)
    private Mercaderia mercaderia;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /* Precio y costo HISTÓRICOS: se copian del catálogo al momento de vender */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoUnitario;

    /* Solo scooters: número de serie (cantidad = 1), base de la garantía */
    @Column(name = "numero_serie", length = 50, unique = true)
    private String numeroSerie;
}
