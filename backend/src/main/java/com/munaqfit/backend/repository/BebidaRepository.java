package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Bebida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BebidaRepository extends JpaRepository<Bebida, Long> {

    List<Bebida> findByActivoTrue();

    List<Bebida> findByCategoria(String categoria);

    List<Bebida> findByNombreContainingIgnoreCase(String nombre);
}
