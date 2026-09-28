package com.munaqfit.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Relacion N-N producto <-> proveedor.
 *
 * Ademas de la pareja de claves guarda dos atributos propios del vinculo:
 * el precio al que ese proveedor vende el insumo (precio_unitario) y si
 * es el proveedor principal (es_principal). Tener atributos la convierte
 * en una entidad asociativa, no solo en una tabla puente.
 */
@Entity
@Table(name = "producto_proveedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoProveedor {

    @EmbeddedId
    private ProductoProveedorId id = new ProductoProveedorId();

    /** Producto del vinculo; el lado opuesto lo carga Producto. */
    @JsonIgnore
    @MapsId("productoId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @MapsId("proveedorId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 4)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = true;
}