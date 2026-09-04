package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.VisitaCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitaClienteRepository extends JpaRepository<VisitaCliente, Long> {

    List<VisitaCliente> findByClienteFidelidadId(Long clienteId);
}
