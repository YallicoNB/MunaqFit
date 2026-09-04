package com.munaqfit.backend.service;

import com.munaqfit.backend.config.JwtConfig;
import com.munaqfit.backend.dto.LoginRequest;
import com.munaqfit.backend.dto.LoginResponse;
import com.munaqfit.backend.exception.CustomExceptions;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.UsuarioRepository;
import com.munaqfit.backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_INTENTOS = 3;
    private static final int BLOQUEO_MINUTOS = 5;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JwtConfig jwtConfig) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtConfig = jwtConfig;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByDni(request.getDni())
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido - DNI no registrado: {}", request.getDni());
                    return new CustomExceptions.UnauthorizedException("DNI o contraseña incorrectos");
                });

        if (usuario.getEstado() == Usuario.EstadoUsuario.INACTIVO) {
            throw new CustomExceptions.UnauthorizedException("DNI o contraseña incorrectos");
        }

        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new CustomExceptions.UnauthorizedException(
                    "Demasiados intentos fallidos. Espere 5 minutos para intentar nuevamente");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() == null ? 1 : usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= MAX_INTENTOS) {
                usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(BLOQUEO_MINUTOS));
                usuario.setIntentosFallidos(0);
                usuarioRepository.save(usuario);
                log.warn("Cuenta bloqueada por intentos fallidos: {}", usuario.getDni());
                throw new CustomExceptions.UnauthorizedException(
                        "Demasiados intentos fallidos. Espere 5 minutos para intentar nuevamente");
            }
            usuarioRepository.save(usuario);
            log.warn("Credenciales incorrectas para DNI: {}", usuario.getDni());
            throw new CustomExceptions.UnauthorizedException("DNI o contraseña incorrectos");
        }

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtTokenProvider.generateToken(usuario.getDni(), usuario.getRol().name());
        log.info("Login exitoso - DNI: {}, rol: {}", usuario.getDni(), usuario.getRol().name());

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getDni(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol().name(),
                jwtConfig.getExpirationMs()
        );
    }

    public String solicitarRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.BadRequestException(
                        "Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña"));
        // Simulación: en producción se enviaría por email
        String token = UUID.randomUUID().toString();
        log.info("Simulación de recuperación para {}: token = {}", email, token);
        return token;
    }

    public void resetPassword(String token, String nuevaPassword) {
        // En producción se valida el token de 1 hora en BD. Simulación simplificada.
        if (token == null || token.isBlank()) {
            throw new CustomExceptions.BadRequestException("El enlace ha expirado o es inválido. Solicita un nuevo enlace de recuperación");
        }
        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            throw new CustomExceptions.BadRequestException("La nueva contraseña debe tener mínimo 8 caracteres");
        }
    }
}
