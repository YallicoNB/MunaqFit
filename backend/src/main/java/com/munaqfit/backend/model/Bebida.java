package com.munaqfit.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    /**
     * Antes la categoria era texto suelto (VARCHAR) y no coincidia con la
     * tabla de categorias de insumo. Ahora es una clave foranea real, asi
     * que no puede quedar huerfana ni duplicada por error.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaBebida categoriaBebida;

    /**
     * La categoria se sigue exponiendo como texto plano para no romper el
     * contrato de la API: el frontend recibe "categoria": "Detox" y no un
     * objeto anidado, asi que no necesita ningun cambio.
     */
    @JsonProperty("categoria")
    public String getCategoriaNombre() {
        return categoriaBebida != null ? categoriaBebida.getNombre() : null;
    }

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "tiempo_preparacion")
    private Integer tiempoPreparacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "bebida")
    private List<Receta> recetas = new ArrayList<>();
}
