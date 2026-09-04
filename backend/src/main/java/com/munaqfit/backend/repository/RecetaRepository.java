package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

    List<Receta> findByBebidaId(Long bebidaId);
}
