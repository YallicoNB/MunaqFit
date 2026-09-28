package com.munaqfit.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Clasificacion de las bebidas por beneficio (Detox, Energizante...).
 * Es una taxonomia distinta a la de los insumos: por eso vive en su
 * propia tabla en vez de mezclarse en {@link Categoria}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categoria_bebida")
public class CategoriaBebida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @JsonIgnore
    @OneToMany(mappedBy = "categoriaBebida")
    private List<Bebida> bebidas = new ArrayList<>();
}
