package com.voltiopedal.apiinventario.cliente;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity 
@Table (name = "cliente")
@Getter @Setter @NoArgsConstructor 
public class Cliente {

    /*ID CLIENTE */
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id_cliente")
    private Integer idCliente;


    /*TIPO DOCUMENTO */
    @Column (name = "tipo_documento", nullable = false, length = 20, unique = true)
    private String tipoDocumento;

    /*NUMERO DOCUMENTO */
    @Column (name = "numero_documento", nullable = false, length = 20, unique = true)
    private String numeroDocumento;

    /*NOMBRES */
    @Column (name = "nombres", nullable = false, length = 100, unique = true)
    private String nombres;

    /*APELLIDOS */
    @Column (name = "apellidos", nullable = false, length = 100, unique = true)
    private String apellidos;

    /*TELEFONO */
    @Column (name = "telefono", nullable = false, length = 20, unique = true)
    private String telefono;

}
