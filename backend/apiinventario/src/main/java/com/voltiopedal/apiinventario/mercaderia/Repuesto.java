package com.voltiopedal.apiinventario.mercaderia;

import com.voltiopedal.apiinventario.tiporepuesto.TipoRepuesto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "repuesto")
@DiscriminatorValue("REPUESTO")
@PrimaryKeyJoinColumn(name = "id_mercaderia")
@Getter @Setter @NoArgsConstructor
public class Repuesto extends Mercaderia {

    /* EAGER: el tipo de repuesto se carga siempre junto con el repuesto (tabla pequeña) */
    /* Así no falla al leerlo fuera de la consulta, porque open-in-view está desactivado */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tipo_repuesto", nullable = false)
    private TipoRepuesto tipoRepuesto;

    @Column(name = "modelos_compatibles", nullable = false, length = 255)
    private String modelosCompatibles;
}
