package com.munaqfit.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bloqueo optimista (V4): si dos ventas descuentan el mismo insumo a
     * la vez, la segunda transaccion recibe 409 en vez de dejar el stock
     * incoherente.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /**
     * Proveedores del insumo (N-N vía producto_proveedor).
     * La serializacion de esta lista va por ProductoResponseDTO: la entidad
     * no se expone al JSON para no arrastrar lazy loading ni recursion.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoProveedor> productoProveedores = new ArrayList<>();

    @Column(name = "stock_actual", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "stock_critico", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockCritico = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private UnidadMedida unidadMedida = UnidadMedida.UNIDAD;

    @Column(name = "costo_unitario", nullable = false, precision = 10, scale = 4)
    private BigDecimal costoUnitario = BigDecimal.ZERO;

    // precio_venta se elimino de la tabla: el insumo no se vende solo, se
    // vende a traves de una bebida, y ese precio vive en bebida.precio.
    // Tenerlo aqui era una columna que nadie llenaba.

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "ultima_reposicion")
    private LocalDateTime ultimaReposicion;

    @JsonIgnore
    @OneToMany(mappedBy = "producto")
    private List<MovimientoInventario> movimientos = new ArrayList<>();

    public enum UnidadMedida {
        KG, G, L, ML, UNIDAD
    }
}
