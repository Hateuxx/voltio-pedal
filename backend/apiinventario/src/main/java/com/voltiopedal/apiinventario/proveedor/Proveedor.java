package com.voltiopedal.apiinventario.proveedor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name= "proveedor")
@Getter @Setter @NoArgsConstructor 
public class Proveedor {
    /* 1. Integer llega hasta ~2.1 mil millones */
    /*    Porque en MySQL un INT UNSIGNED llega hasta ~4.2 mil millones por ende se va utilizar Integer pq coincide con INT por ser corelacionales */
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    /* 2. CHAR(11) NOT NULL -> String*/
    @Column(name = "ruc" , nullable = false, length = 11, unique = true)
    private String ruc;

    /* 3. VARCHAR(255) NOT NULL -> String */
    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;

    /* 4. VARCHAR(100) NULL -> String */
    @Column(name = "contacto", length = 100)
    private String contacto;

    /* 5. VARCHAR(20) NULL -> String */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /* 6. VARCHAR(100) NULL -> String */
    @Column(name = "rubro", length = 100)
    private String rubro;

    /* 7. VARCHAR(20) NOT NULL -> String */
    @Column(name = "frecuencia_entrega", nullable = false, length = 20)
    private String frecuenciaEntrega;

    /* 8.TINYINT(1) NOT NULL DEFAULT 1 -> Boolean */
    /* Se inicializaen true que coincida con DEFAULT 1 de la bd */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
