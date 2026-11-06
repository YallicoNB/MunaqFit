package com.munaqfit.backend.controller;

import com.munaqfit.backend.dto.LoginRequest;
import com.munaqfit.backend.dto.LoginResponse;
import com.munaqfit.backend.security.JwtTokenProvider;
import com.munaqfit.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Login, refresh token y recuperacion de contrasena")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** Publico: es justamente aqui donde se obtiene el token. */
    @SecurityRequirements
    @Operation(summary = "Inicia sesion y devuelve access + refresh token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Publico: el refresh token es la credencial. Devuelve un access token
     * nuevo y rota el refresh. El frontend lo llama de forma transparente
     * cuando una peticion responde 401.
     */
    @SecurityRequirements
    @Operation(summary = "Renueva el access token con un refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.refrescarToken(body.get("refreshToken")));
    }

    /** Publico: la respuesta es identica exista o no el correo. */
    @SecurityRequirements
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String token = authService.solicitarRecuperacion(body.getOrDefault("email", ""));
        return ResponseEntity.ok(Map.of(
                "message", "Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña",
                "token", token
        ));
    }

    /** Publico: el token de un solo uso llega por correo, no por cabecera. */
    @SecurityRequirements
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        authService.resetPassword(body.getOrDefault("token", ""), body.getOrDefault("newPassword", ""));
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));
    }

    /**
     * Comprueba únicamente si el token JWT recibido es válido.
     * Devuelve 200 si el token es correcto, 401 si falta, está expirado o es inválido.
     * Pensado para que el frontend valide la sesión al cargar la aplicación.
     */
    @GetMapping("/validar-token")
    public ResponseEntity<?> validarToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "mensaje", "No se envió el token de autenticación"));
        }

        String token = authorization.substring(7).trim();

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "mensaje", "El token es inválido o ha expirado"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("valido", true);
        response.put("dni", jwtTokenProvider.getDniFromToken(token));
        response.put("rol", jwtTokenProvider.getRolFromToken(token));
        return ResponseEntity.ok(response);
    }
}
