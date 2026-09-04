package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByVentaId(Long ventaId);
}
