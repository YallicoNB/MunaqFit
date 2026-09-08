package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.munaqfit.backend.dto.RankingBebidaDTO;
import com.munaqfit.backend.model.Venta.EstadoVenta;
import java.time.LocalDateTime;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaId(Long ventaId);

    //Consulta para agrupar bebidas, sumar sus ventas, cuanto generaron y empaquetarlo en el RankingBebidaDTO

    @Query("SELECT new com.munaqfit.backend.dto.RankingBebidaDTO(d.bebida.id, d.bebida.nombre, SUM(d.cantidad), SUM(d.subtotal)) " +
           "FROM DetalleVenta d " +
           "WHERE d.venta.fechaHora BETWEEN :inicio AND :fin AND d.venta.estado = :estado " +
           "GROUP BY d.bebida.id, d.bebida.nombre " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<RankingBebidaDTO> findRankingBebidas(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fin") LocalDateTime fin, 
        @Param("estado") EstadoVenta estado
    );
}
