package com.munaqfit.backend.controller;

import com.munaqfit.backend.dto.LoginRequest;
import com.munaqfit.backend.dto.LoginResponse;
import com.munaqfit.backend.security.JwtTokenProvider;
import com.munaqfit.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String token = authService.solicitarRecuperacion(body.getOrDefault("email", ""));
        return ResponseEntity.ok(Map.of(
                "message", "Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña",
                "token", token
        ));
    }

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
