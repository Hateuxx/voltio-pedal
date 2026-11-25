package com.voltiopedal.apiinventario.mercaderia;

import com.voltiopedal.apiinventario.proveedor.Proveedor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity 
@Table (name = "mercaderia")
/* Herencia JOINED: los datos comunes están en "mercaderia" y los propios de cada tipo en su tabla */
@Inheritance (strategy = InheritanceType.JOINED)
/* La columna "tipo" indica a qué subclase pertenece cada fila (SCOOTER, REPUESTO o ACCESORIO) */
@DiscriminatorColumn (name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
@Getter @Setter @NoArgsConstructor 

/* abstract porque nunca va a existir solo mercaderia, es decir, siempre es uno de los 3 tipos */
public abstract class Mercaderia {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name ="id_mercaderia")
    private Integer idMercaderia;

    @Column (name = "sku", nullable = false, length = 50, unique = true)
    private String sku;

    @Column (name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column (name = "stock", nullable = false)
    private Integer stock = 0;

    @Column (name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column (name = "precio_costo", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioCosto;

    @Column (name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso = LocalDateTime.now();

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    
}
