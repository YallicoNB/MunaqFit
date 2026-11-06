package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Listado paginado de inventario (tarea 2.5) con busqueda por nombre
     * y filtro por categoria. La condicion de stock bajo es opcional.
     */
    @Query("SELECT p FROM Producto p WHERE " +
           "(:texto IS NULL OR :texto = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId) " +
           "AND (:soloBajo = FALSE OR p.stockActual <= p.stockMinimo)")
    Page<Producto> buscarInventario(@Param("texto") String texto,
                                    @Param("categoriaId") Long categoriaId,
                                    @Param("soloBajo") boolean soloBajo,
                                    Pageable pageable);
}
