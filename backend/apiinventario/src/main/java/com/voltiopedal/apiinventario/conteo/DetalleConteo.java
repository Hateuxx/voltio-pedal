package com.voltiopedal.apiinventario.conteo;

import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_conteo")
@Getter @Setter @NoArgsConstructor
public class DetalleConteo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_conteo")
    private Integer idDetalleConteo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conteo", nullable = false)
    private ConteoInventario conteo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mercaderia", nullable = false)
    private Mercaderia mercaderia;

    /* FOTO del stock del sistema en el momento de contar */
    @Column(name = "stock_sistema", nullable = false)
    private Integer stockSistema;

    @Column(name = "stock_fisico", nullable = false)
    private Integer stockFisico;

    @Column(name = "ajustado", nullable = false)
    private Boolean ajustado = false;

    /* La columna "diferencia" la calcula MySQL (GENERATED STORED); en Java la calculamos igual sin mapearla */
    public int getDiferencia() {
        return stockFisico - stockSistema;
    }
}