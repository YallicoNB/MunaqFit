package com.munaqfit.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bloqueo optimista (V4): si dos empleados cobran o modifican la
     * misma venta a la vez, el segundo UPDATE falle con 409 en vez de
     * sobrescribir el estado de un cobro.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    private Integer mesa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_venta", nullable = false, length = 20)
    private TipoVenta tipoVenta = TipoVenta.LOCAL;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Tasa de IGV que se aplico a ESTA venta. Es una instantanea: si
     * mañana la tasa sube, los reportes de hoy siguen siendo correctos.
     * El valor vigente se lee de la tabla parametro.
     */
    @Column(name = "igv_tasa", nullable = false, precision = 5, scale = 4)
    private BigDecimal igvTasa = new BigDecimal("0.1800");

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @Column(length = 255)
    private String notas;

    /** Identificador legible del ticket (PED-000001). Obligatorio y unico. */
    @Column(name = "numero_pedido", nullable = false, length = 30)
    private String numeroPedido;

    @JsonIgnore
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "venta", cascade = CascadeType.ALL)
    private Pago pago;

    public enum TipoVenta {
        LOCAL, DELIVERY
    }

    public enum EstadoVenta {
        PENDIENTE, PAGADO, CANCELADO
    }
}
