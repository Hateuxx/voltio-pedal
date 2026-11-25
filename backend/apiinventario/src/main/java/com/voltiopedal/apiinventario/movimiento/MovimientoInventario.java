package com.voltiopedal.apiinventario.movimiento;

import com.voltiopedal.apiinventario.mercaderia.Mercaderia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_inventario")
@Getter @Setter @NoArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoMovimiento tipo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_resultante", nullable = false)
    private Integer stockResultante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mercaderia", nullable = false)
    private Mercaderia mercaderia;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "id_servicio_tecnico")
    private Integer idServicioTecnico;

    @Column(name = "id_ingreso")
    private Integer idIngreso;

    @Column(name = "id_conteo")
    private Integer idConteo;

    @Column(name = "observacion", length = 255)
    private String observacion;
}