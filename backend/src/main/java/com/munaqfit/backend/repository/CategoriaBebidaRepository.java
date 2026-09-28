package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.CategoriaBebida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaBebidaRepository extends JpaRepository<CategoriaBebida, Long> {

    Optional<CategoriaBebida> findByNombreIgnoreCase(String nombre);
}
