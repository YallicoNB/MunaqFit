package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.Venta.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByUsuarioId(Long usuarioId);

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findByUsuarioIdAndFechaHoraBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    long countByEstado(EstadoVenta estado);
}
