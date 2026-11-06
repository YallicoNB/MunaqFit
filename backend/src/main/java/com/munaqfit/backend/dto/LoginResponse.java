package com.munaqfit.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tokens y datos del usuario autenticado.")
public class LoginResponse {

    private String token;
    private String refreshToken;
    private String tipo;
    private Long idUsuario;
    private String dni;
    private String nombreCompleto;
    private String email;
    private String rol;
    private long expiraEn;
}
