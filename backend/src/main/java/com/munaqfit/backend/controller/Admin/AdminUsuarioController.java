package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.dto.UsuarioCreateRequest;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // REQ-007: Listar todos los empleados
    @GetMapping
    public ResponseEntity<List<Usuario>> listarEmpleados() {
        // Asumiendo que el enum se llama Rol.EMPLEADO
        return ResponseEntity.ok(usuarioRepository.findByRol(Usuario.Rol.EMPLEADO));
    }

    // REQ-006: Crear un nuevo empleado
    @PostMapping
    public ResponseEntity<?> crearEmpleado(@RequestBody UsuarioCreateRequest request) {
        if (usuarioRepository.existsByDni(request.getDni())) {
            return ResponseEntity.badRequest().body("Error: El DNI ya está registrado.");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Error: El correo ya está registrado.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(request.getNombreCompleto());
        nuevoUsuario.setDni(request.getDni());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        
        nuevoUsuario.setRol(Usuario.Rol.EMPLEADO); // Rol fijo de empleado
        nuevoUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO); // Estado inicial
        nuevoUsuario.setFechaCreacion(LocalDateTime.now());

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // REQ-008: Eliminar (Soft Delete) a un empleado
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEmpleado(@PathVariable Long id) {
        // En una app real, aqui validamos que el adminId no sea igual al ID a eliminar
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setEstado(Usuario.EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);
        
        return ResponseEntity.ok("Empleado eliminado (inactivado) exitosamente.");
    }
}