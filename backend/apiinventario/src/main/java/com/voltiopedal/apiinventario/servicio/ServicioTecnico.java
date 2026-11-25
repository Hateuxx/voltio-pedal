package com.voltiopedal.apiinventario.servicio;

import com.voltiopedal.apiinventario.cliente.Cliente;
import com.voltiopedal.apiinventario.mercaderia.Scooter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servicio_tecnico")
@Getter @Setter @NoArgsConstructor
public class ServicioTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio_tecnico")
    private Integer idServicioTecnico;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "numero_serie", nullable = false, length = 50)
    private String numeroSerie;

    @Column(name = "falla_reportada", nullable = false, columnDefinition = "TEXT")
    private String fallaReportada;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoServicio estado = EstadoServicio.INGRESADO;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "meses_garantia")
    private Integer mesesGarantia;

    @Column(name = "cubierto_por_garantia", nullable = false)
    private Boolean cubiertoPorGarantia = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    /* FK a scooter(id_mercaderia): solo se reparan scooters */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_scooter", nullable = false)
    private Scooter scooter;

    @Column(name = "id_tecnico")
    private Integer idTecnico;

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idDetalleServicio ASC")
    private List<DetalleServicio> repuestos = new ArrayList<>();

    public void agregarRepuesto(DetalleServicio detalle) {
        detalle.setServicio(this);
        repuestos.add(detalle);
    }
}
