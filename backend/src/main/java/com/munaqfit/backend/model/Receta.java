package com.munaqfit.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "receta")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_id", nullable = false)
    private Bebida bebida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /**
     * Cantidad en la unidad canonica del producto (producto.unidad_medida).
     * La columna 'unidad' se elimino: al duplicar la unidad habia que
     * convertir G<->KG y ML<->L, y una receta podia contradecir al insumo.
     */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "paso_instruccion", columnDefinition = "TEXT")
    private String pasoInstruccion;
}
