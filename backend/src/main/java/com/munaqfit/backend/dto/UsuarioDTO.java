package com.munaqfit.backend.dto;

import com.munaqfit.backend.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 12, message = "El DNI debe tener entre 8 y 12 caracteres")
    private String dni;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String password;

    private String rol;
    private String estado;
    private LocalDateTime ultimoLogin;
    private LocalDateTime fechaCreacion;

    public static UsuarioDTO fromEntity(Usuario u) {
        return new UsuarioDTO(
                u.getId(),
                u.getDni(),
                u.getNombreCompleto(),
                u.getEmail(),
                null,
                u.getRol() != null ? u.getRol().name() : null,
                u.getEstado() != null ? u.getEstado().name() : null,
                u.getUltimoLogin(),
                u.getFechaCreacion()
        );
    }
}
