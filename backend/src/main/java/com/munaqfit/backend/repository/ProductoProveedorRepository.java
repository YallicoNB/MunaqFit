package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.ProductoProveedor;
import com.munaqfit.backend.model.ProductoProveedorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a la tabla puente producto<->proveedor.
 * La clave es compuesta (ProductoProveedorId), por eso el repositorio
 * extiende JpaRepository con esa clave.
 */
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, ProductoProveedorId> {

    /** Los proveedores de un insumo, para el DTO de producto. */
    List<ProductoProveedor> findByProducto_Id(Long productoId);

    /** Los insumos que vende un proveedor (bandeja del proveedor). */
    List<ProductoProveedor> findByProveedor_Id(Long proveedorId);
}