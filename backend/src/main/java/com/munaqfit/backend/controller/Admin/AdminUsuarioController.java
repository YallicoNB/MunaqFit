package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.dto.CambiarRolRequest;
import com.munaqfit.backend.dto.UsuarioCreateRequest;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.munaqfit.backend.dto.UsuarioDTO;
import jakarta.validation.Valid;

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

    // REQ-007: Listar todos los usuarios (incluye administradores para poder gestionar su rol)
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
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
    public ResponseEntity<?> eliminarEmpleado(@PathVariable Long id, Authentication authentication) {
        
        // 1. Obtenemos quién está haciendo la petición
        String dniAdmin = authentication.getName();
        Usuario adminLogueado = usuarioRepository.findByDni(dniAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        
        // 2. VALIDACIÓN REQ-008: Impedir que se elimine a sí mismo
        if (adminLogueado.getId().equals(id)) {
            return ResponseEntity.badRequest().body("Error de seguridad: No puedes eliminar tu propia cuenta de administrador.");
        }

        // 3. Si pasa la validación, procedemos a eliminar (inactivar)
        Usuario usuarioAEliminar = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuarioAEliminar.setEstado(Usuario.EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuarioAEliminar);
        
        return ResponseEntity.ok("Empleado eliminado (inactivado) exitosamente.");
    }

    @PutMapping("/{id}")
    public ResponseEntity actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioCreateRequest request) {
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validación de DNI: Si cambia, verificamos que no exista en la BD
        if (!usuario.getDni().equals(request.getDni()) && usuarioRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("El DNI ingresado ya está registrado en otro usuario");
        }

        // Validación de Email: Prevenimos duplicados también en el correo
        if (!usuario.getEmail().equals(request.getEmail()) && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo ingresado ya está registrado en otro usuario");
        }

        // Actualizamos los datos básicos
        usuario.setDni(request.getDni());
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        
        // Actualizamos la contraseña solo si el frontend envía una nueva
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Guardamos en BD
        Usuario guardado = usuarioRepository.save(usuario);
        
        // Retornamos usando tu propio método mapeador
        return ResponseEntity.ok(UsuarioDTO.fromEntity(guardado));
    }

    /**
     * Cambia el rol de un usuario (ADMIN <-> EMPLEADO).
     */
    @PutMapping("/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable Long id,
                                        @RequestBody CambiarRolRequest request,
                                        Authentication authentication) {

        // 1. Validar el rol recibido
        if (request.getRol() == null || request.getRol().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Debes indicar un rol.");
        }

        Usuario.Rol nuevoRol;
        try {
            nuevoRol = Usuario.Rol.valueOf(request.getRol().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Error: Rol inválido. Usa ADMIN o EMPLEADO.");
        }

        // 2. Buscar el usuario objetivo
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 3. No permitir que el admin se quite su propio rol de administrador
        String dniAdmin = authentication.getName();
        Usuario adminLogueado = usuarioRepository.findByDni(dniAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        if (adminLogueado.getId().equals(id) && nuevoRol != Usuario.Rol.ADMIN) {
            return ResponseEntity.badRequest()
                    .body("Error de seguridad: No puedes quitarte tu propio rol de administrador.");
        }

        // 4. No dejar el sistema sin ningún administrador activo
        if (usuario.getRol() == Usuario.Rol.ADMIN && nuevoRol != Usuario.Rol.ADMIN) {
            long adminsActivos = usuarioRepository.findByRol(Usuario.Rol.ADMIN).stream()
                    .filter(u -> u.getEstado() == Usuario.EstadoUsuario.ACTIVO)
                    .count();
            if (adminsActivos <= 1) {
                return ResponseEntity.badRequest()
                        .body("Error: Debe existir al menos un administrador activo.");
            }
        }

        // 5. Guardar el nuevo rol
        usuario.setRol(nuevoRol);
        Usuario guardado = usuarioRepository.save(usuario);

        return ResponseEntity.ok(UsuarioDTO.fromEntity(guardado));
    }
}