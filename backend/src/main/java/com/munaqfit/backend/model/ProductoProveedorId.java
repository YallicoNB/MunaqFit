package com.munaqfit.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clave compuesta de la tabla puente producto_proveedor.
 * Un producto puede comprarse a varios proveedores y un proveedor puede
 * vender varios productos; la identidad es la pareja (producto, proveedor).
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoProveedorId implements Serializable {

    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "proveedor_id")
    private Long proveedorId;
}