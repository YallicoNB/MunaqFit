package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.Venta.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByUsuarioId(Long usuarioId);

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findByUsuarioIdAndFechaHoraBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    long countByEstado(EstadoVenta estado);

    //Calculos necesarios para los dashboards

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaHora BETWEEN :inicio AND :fin AND v.estado = :estado")
    BigDecimal sumVentasEnRangoYEstado(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fin") LocalDateTime fin, 
        @Param("estado") EstadoVenta estado
    );

    List<Venta> findAllByOrderByFechaHoraDesc();

    /**
     * Ultimos numeros de pedido emitidos hoy, para continuar la secuencia
     * del dia (PED-YYYYMMDD-####). El prefijo ya trae la fecha, asi que
     * con ordenar descendentemente y limitarlo a 1 alcanza el mas reciente.
     * JPQL explicito porque un findTop derivado seleccionaba la entidad
     * entera en vez de proyectar solo la columna de texto.
     */
    @Query("SELECT v.numeroPedido FROM Venta v WHERE v.numeroPedido LIKE :prefijo ORDER BY v.numeroPedido DESC")
    List<String> findUltimosNumeroPedido(@Param("prefijo") String prefijo, Pageable pageable);

}
