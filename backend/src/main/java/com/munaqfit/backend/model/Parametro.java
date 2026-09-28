package com.munaqfit.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Configuracion del negocio que antes vivia hardcodeada en el codigo,
 * por ejemplo la tasa de IGV. Permite cambiarla sin recompilar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parametro")
public class Parametro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dato", nullable = false, length = 20)
    private TipoDato tipoDato = TipoDato.TEXTO;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "vigencia_desde", nullable = false)
    private LocalDateTime vigenciaDesde = LocalDateTime.now();

    public enum TipoDato {
        DECIMAL, ENTERO, TEXTO, BOOLEANO
    }
}
