package com.munaqfit.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    /**
     * No existe monto_total: se deriva de venta.total. Guardarlo aqui
     * duplicaba el dato y permitia que ambos dejaran de coincidir.
     */
    @Column(name = "monto_pagado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "monto_cambio", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoCambio = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false, length = 20)
    private TipoPago tipoPago;

    @Column(name = "numero_operacion", length = 50)
    private String numeroOperacion;

    @Column(length = 50)
    private String banco;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    public enum TipoPago {
        EFECTIVO, YAPE, PLIN, TRANSFERENCIA, QR
    }

    public enum EstadoPago {
        PENDIENTE, COMPLETADO
    }
}
