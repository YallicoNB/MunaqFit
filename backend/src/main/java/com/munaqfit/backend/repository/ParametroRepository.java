package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Parametro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametroRepository extends JpaRepository<Parametro, Long> {

    Optional<Parametro> findByCodigo(String codigo);
}
