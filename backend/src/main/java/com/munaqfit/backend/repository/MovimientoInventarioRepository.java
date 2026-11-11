package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.MovimientoInventario;
import com.munaqfit.backend.model.MovimientoInventario.TipoMovimiento;
import com.munaqfit.backend.dto.KardexFilaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByProductoId(Long productoId);

    List<MovimientoInventario> findByTipoMovimiento(TipoMovimiento tipo);

    List<MovimientoInventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT new com.munaqfit.backend.dto.KardexFilaDTO(" +
           "m.fechaMovimiento, p.nombre, CAST(m.tipoMovimiento AS string), m.cantidad, m.stockAnterior, m.stockNuevo, m.motivo, u.nombreCompleto) " +
           "FROM MovimientoInventario m " +
           "JOIN m.producto p " +
           "LEFT JOIN m.usuario u " +
           "WHERE (:fechaDesde IS NULL OR m.fechaMovimiento >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR m.fechaMovimiento <= :fechaHasta) " +
           "AND (:productoId IS NULL OR p.id = :productoId) " +
           "AND (:tipo IS NULL OR m.tipoMovimiento = :tipo) " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId) " +
           "ORDER BY m.fechaMovimiento DESC")
    List<KardexFilaDTO> consultarKardexFiltros(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("productoId") Long productoId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("usuarioId") Long usuarioId
    );

    /**
     * Misma consulta que {@link #consultarKardexFiltros} pero paginada
     * (tarea 2.5). El countQuery es explicito porque la proyeccion usa un
     * constructor DTO y Spring Data no puede derivarlo solo.
     */
    @Query(value = "SELECT new com.munaqfit.backend.dto.KardexFilaDTO(" +
           "m.fechaMovimiento, p.nombre, CAST(m.tipoMovimiento AS string), m.cantidad, m.stockAnterior, m.stockNuevo, m.motivo, u.nombreCompleto) " +
           "FROM MovimientoInventario m " +
           "JOIN m.producto p " +
           "LEFT JOIN m.usuario u " +
           "WHERE (:fechaDesde IS NULL OR m.fechaMovimiento >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR m.fechaMovimiento <= :fechaHasta) " +
           "AND (:productoId IS NULL OR p.id = :productoId) " +
           "AND (:tipo IS NULL OR m.tipoMovimiento = :tipo) " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId)",
           countQuery = "SELECT COUNT(m) FROM MovimientoInventario m " +
           "JOIN m.producto p " +
           "LEFT JOIN m.usuario u " +
           "WHERE (:fechaDesde IS NULL OR m.fechaMovimiento >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR m.fechaMovimiento <= :fechaHasta) " +
           "AND (:productoId IS NULL OR p.id = :productoId) " +
           "AND (:tipo IS NULL OR m.tipoMovimiento = :tipo) " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId)")
    Page<KardexFilaDTO> consultarKardexFiltrosPaginado(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("productoId") Long productoId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );
}