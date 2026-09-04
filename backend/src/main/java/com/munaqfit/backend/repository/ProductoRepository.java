package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByStockActualLessThanEqual(BigDecimal stock);

    List<Producto> findByStockActualLessThanEqualAndStockMinimoGreaterThan(BigDecimal stock, BigDecimal minimo);

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByProveedorId(Long proveedorId);
}
