package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.model.Usuario.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByDni(String dni);

    Optional<Usuario> findByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    List<Usuario> findByEstado(EstadoUsuario estado);

    List<Usuario> findByRol(Usuario.Rol rol);

    List<Usuario> findByNombreCompletoContainingIgnoreCase(String nombre);
}
