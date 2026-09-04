package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);

    List<Proveedor> findByRucContaining(String ruc);

    Optional<Proveedor> findByRuc(String ruc);
}
