package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.dto.CambiarRolRequest;
import com.munaqfit.backend.dto.PageResponse;
import com.munaqfit.backend.dto.UsuarioCreateRequest;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.UsuarioRepository;
import com.munaqfit.backend.util.Sanitizacion;
import com.munaqfit.backend.util.Ordenamiento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(name = "Admin - Usuarios", description = "Gestion de empleados y roles")
public class AdminUsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // REQ-007: Listar todos los usuarios paginados (contrato de la etapa 3, seccion 5)
    @Operation(summary = "Lista usuarios paginados con busqueda por DNI/nombre y filtro por estado")
    @GetMapping
    public ResponseEntity<PageResponse<UsuarioDTO>> listarUsuarios(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "fechaCreacion,desc") String orden) {

        int size = Math.min(Math.max(tamano, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), size,
                Ordenamiento.parsear(orden,
                        java.util.Set.of("fechaCreacion", "nombreCompleto", "dni", "estado",
                                "ultimoLogin", "rol"),
                        "fechaCreacion", Sort.Direction.DESC));

        Usuario.EstadoUsuario estadoEnum = null;
        if (estado != null && !estado.isBlank()) {
            try {
                estadoEnum = Usuario.EstadoUsuario.valueOf(estado.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Estado desconocido: se ignora el filtro en lugar de romper la lista
            }
        }

        String texto = Sanitizacion.texto(busqueda);
        Page<Usuario> page = usuarioRepository.buscarPaginado(texto, estadoEnum, pageable);
        return ResponseEntity.ok(PageResponse.desde(page, UsuarioDTO::fromEntity));
    }

    // REQ-006: Crear un nuevo empleado
    @Operation(summary = "Crea un empleado (rol EMPLEADO, estado ACTIVO)")
    @PostMapping
    public ResponseEntity<?> crearEmpleado(@Valid @RequestBody UsuarioCreateRequest request) {
        String dni = Sanitizacion.dni(request.getDni());
        String email = Sanitizacion.email(request.getEmail());
        String nombre = Sanitizacion.texto(request.getNombreCompleto());

        if (dni == null || dni.isBlank() || email == null || email.isBlank()
                || nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest().body("Error: DNI, nombre y correo son obligatorios.");
        }

        if (usuarioRepository.existsByDni(dni)) {
            return ResponseEntity.badRequest().body("Error: El DNI ya está registrado.");
        }
        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Error: El correo ya está registrado.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(nombre);
        nuevoUsuario.setDni(dni);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        
        nuevoUsuario.setRol(Usuario.Rol.EMPLEADO); // Rol fijo de empleado
        nuevoUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO); // Estado inicial
        nuevoUsuario.setFechaCreacion(LocalDateTime.now());

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO.fromEntity(guardado));
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

    @Operation(summary = "Actualiza los datos basicos de un usuario")
    @PutMapping("/{id}")
    public ResponseEntity actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioCreateRequest request) {
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String dni = Sanitizacion.dni(request.getDni());
        String email = Sanitizacion.email(request.getEmail());
        String nombre = Sanitizacion.texto(request.getNombreCompleto());

        // Validación de DNI: Si cambia, verificamos que no exista en la BD
        if (!usuario.getDni().equals(dni) && usuarioRepository.existsByDni(dni)) {
            throw new RuntimeException("El DNI ingresado ya está registrado en otro usuario");
        }

        // Validación de Email: Prevenimos duplicados también en el correo
        if (!usuario.getEmail().equals(email) && usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El correo ingresado ya está registrado en otro usuario");
        }

        // Actualizamos los datos básicos
        usuario.setDni(dni);
        usuario.setNombreCompleto(nombre);
        usuario.setEmail(email);
        
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
                                        @Valid @RequestBody CambiarRolRequest request,
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