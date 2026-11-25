package com.voltiopedal.apiinventario.mercaderia;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accesorio")
@DiscriminatorValue("ACCESORIO")
@PrimaryKeyJoinColumn(name = "id_mercaderia")
@Getter @Setter @NoArgsConstructor
public class Accesorio extends Mercaderia {

    @Column(name = "talla", length = 20)
    private String talla;

    @Column(name = "color", length = 50)
    private String color;
}
