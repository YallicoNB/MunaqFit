package com.munaqfit.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bebida")
public class Bebida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio = new BigDecimal("16.00");

    @Column(length = 50)
    private String categoria;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "tiempo_preparacion")
    private Integer tiempoPreparacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "bebida")
    private List<Receta> recetas = new ArrayList<>();
}
