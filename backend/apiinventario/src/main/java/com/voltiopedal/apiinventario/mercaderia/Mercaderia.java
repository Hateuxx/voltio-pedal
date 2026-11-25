package com.voltiopedal.apiinventario.mercaderia;

import com.voltiopedal.apiinventario.proveedor.Proveedor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* Supertipo: cada fila es un SKU del catálogo. Abstracta: nunca existe una "mercadería" sin tipo */
@Entity
@Table(name = "mercaderia")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
@Getter @Setter @NoArgsConstructor
public abstract class Mercaderia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mercaderia")
    private Integer idMercaderia;

    @Column(name = "sku", nullable = false, length = 50, unique = true)
    private String sku;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    /* Solo cambia con movimientos de inventario (ingresos, ventas, consumo) */
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    /* DECIMAL(12,4) -> BigDecimal: nunca double para dinero */
    @Column(name = "precio_costo", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioCosto;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;
}
