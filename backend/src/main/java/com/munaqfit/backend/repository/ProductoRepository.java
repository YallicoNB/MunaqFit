package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByStockActualLessThanEqual(BigDecimal stock);

    List<Producto> findByStockActualLessThanEqualAndStockMinimoGreaterThan(BigDecimal stock, BigDecimal minimo);

    List<Producto> findByCategoriaId(Long categoriaId);

    //Consultas "personalizadas"

    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockCritico")
    List<Producto> findStockCritico();

    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo")
    List<Producto> findStockBajo();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stockActual <= p.stockCritico")
    long countStockCritico();

    @Query("SELECT COALESCE(SUM(p.stockActual * p.costoUnitario), 0) FROM Producto p")
    BigDecimal calcularValorizacionTotal();
}
