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
    @Enumerated(EnumType.STRING)
    @Column (name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumentoCliente tipoDocumento;

    /*NUMERO DOCUMENTO */
    @Column (name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    /*NOMBRES */
    @Column (name = "nombres", nullable = false, length = 100)
    private String nombres;

    /*APELLIDOS */
    @Column (name = "apellidos", length = 100)
    private String apellidos;

    /*TELEFONO */
    @Column (name = "telefono", length = 20)
    private String telefono;

}
