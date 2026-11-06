package com.munaqfit.backend.repository;

import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.model.Usuario.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Busqueda paginada para la tabla de usuarios (tareas 2.5 y 3.8):
     * texto libre sobre DNI o nombre y filtro opcional por estado.
     */
    @Query("SELECT u FROM Usuario u WHERE " +
           "(:texto IS NULL OR :texto = '' " +
           "  OR LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "  OR u.dni LIKE CONCAT('%', :texto, '%')) " +
           "AND (:estado IS NULL OR u.estado = :estado)")
    Page<Usuario> buscarPaginado(@Param("texto") String texto,
                                 @Param("estado") EstadoUsuario estado,
                                 Pageable pageable);
}
