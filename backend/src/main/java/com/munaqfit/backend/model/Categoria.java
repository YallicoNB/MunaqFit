package com.munaqfit.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Sin cascade a proposito. Con CascadeType.ALL, borrar una categoria
     * arrastraba tambien el stock de todos sus productos: la base perdia
     * historial de inventario y pedidos por un DELETE de una fila de
     * configuracion. La FK en producto.categoria_id ya impide borrar una
     * categoria que siga en uso.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "categoria")
    private List<Producto> productos = new ArrayList<>();
}
