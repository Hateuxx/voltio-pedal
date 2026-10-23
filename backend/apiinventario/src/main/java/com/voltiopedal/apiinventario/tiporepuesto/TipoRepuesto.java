package com.voltiopedal.apiinventario.tiporepuesto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;;

@Entity 
@Table (name = "tipo_repuesto")
@Getter @Setter @NoArgsConstructor 
public class TipoRepuesto {

    /*ID REPUESTO*/
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id_tipo_repuesto")
    private Integer idTipoRepuesto;


    /*NOMBRE */
    @Column (name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;
    

    
}
