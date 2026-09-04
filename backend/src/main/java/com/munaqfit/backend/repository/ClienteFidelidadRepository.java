package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.ClienteFidelidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteFidelidadRepository extends JpaRepository<ClienteFidelidad, Long> {

    Optional<ClienteFidelidad> findByTelefono(String telefono);

    List<ClienteFidelidad> findByNombreContainingIgnoreCase(String nombre);
}
