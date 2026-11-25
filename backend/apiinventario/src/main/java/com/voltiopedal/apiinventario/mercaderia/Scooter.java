package com.voltiopedal.apiinventario.mercaderia;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scooter")
/* Cuando "tipo" vale SCOOTER, Hibernate crea un objeto de esta clase */
@DiscriminatorValue("SCOOTER")
/* La clave de esta tabla es la misma id_mercaderia de la tabla madre */
@PrimaryKeyJoinColumn(name = "id_mercaderia")
@Getter
@Setter
@NoArgsConstructor

public class Scooter extends Mercaderia {

    @Column(name = "autonomia_km", nullable = false)
    private Integer autonomiaKm;

    @Column(name = "potencia_watts", nullable = false)
    private Integer potenciaWatts;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "meses_garantia", nullable = false)
    private Integer mesesGarantia;
}