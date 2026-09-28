package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Pago;
import com.munaqfit.backend.model.Pago.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByVentaId(Long ventaId);

    // REQ-030: total cobrado por cada metodo de pago en un rango de fechas.
    // Se suma venta.total (la venta a la que apunta el pago) y no
    // pago.montoTotal, porque esa columna ya no existe: el monto vive en
    // un solo lado, en la venta.
    @Query("SELECT p.tipoPago, SUM(v.total) " +
           "FROM Pago p JOIN p.venta v " +
           "WHERE p.fechaPago BETWEEN :inicio AND :fin AND p.estado = :estado " +
           "GROUP BY p.tipoPago")
    List<Object[]> ingresosPorMetodoPago(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estado") EstadoPago estado);
}
