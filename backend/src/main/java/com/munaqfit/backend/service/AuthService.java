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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_INTENTOS = 3;
    private static final int BLOQUEO_MINUTOS = 5;
    private static final int TOKEN_RECUPERACION_MINUTOS = 60;

    /**
     * Tokens de recuperacion de contrasena pendientes de usar.
     * Se guardan en memoria a proposito: el alcance del proyecto no incluye el envio
     * de correo, asi que no se requiere persistencia. Cada token es de un solo uso.
     */
    private final Map<String, TokenRecuperacion> tokensRecuperacion = new ConcurrentHashMap<>();

    /** Token de recuperacion asociado al DNI del usuario y su fecha de expiracion. */
    private record TokenRecuperacion(String dni, LocalDateTime expira) {}

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

    /**
     * Genera un token de recuperacion para el correo indicado.
     * La respuesta es identica exista o no el correo: de lo contrario se permitiria
     * a un atacante enumerar los correos registrados en el sistema.
     */
    public String solicitarRecuperacion(String email) {
        purgarTokensVencidos();
        String token = UUID.randomUUID().toString();

        Optional<Usuario> encontrado = usuarioRepository.findByEmail(email);
        if (encontrado.isEmpty()) {
            log.info("Recuperacion solicitada para correo no registrado: {}", email);
            return token;
        }

        Usuario usuario = encontrado.get();
        LocalDateTime expira = LocalDateTime.now().plusMinutes(TOKEN_RECUPERACION_MINUTOS);
        tokensRecuperacion.put(token, new TokenRecuperacion(usuario.getDni(), expira));
        // Simulacion: en produccion este token viajaria por correo, no en la respuesta.
        log.info("Recuperacion solicitada para DNI {}. Token {} (expira {})", usuario.getDni(), token, expira);
        return token;
    }

    /**
     * Aplica la nueva contrasena asociada a un token de recuperacion.
     * El token es de un solo uso y se descarta al completar el cambio.
     */
    public void resetPassword(String token, String nuevaPassword) {
        purgarTokensVencidos();

        TokenRecuperacion recuperacion = (token == null || token.isBlank())
                ? null
                : tokensRecuperacion.get(token);

        if (recuperacion == null) {
            throw new CustomExceptions.BadRequestException(
                    "El enlace ha expirado o es inválido. Solicita un nuevo enlace de recuperación");
        }
        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            throw new CustomExceptions.BadRequestException("La nueva contraseña debe tener mínimo 8 caracteres");
        }

        Usuario usuario = usuarioRepository.findByDni(recuperacion.dni())
                .orElseThrow(() -> new CustomExceptions.BadRequestException(
                        "El enlace ha expirado o es inválido. Solicita un nuevo enlace de recuperación"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        // Restablecer la contrasena tambien libera el bloqueo por intentos fallidos.
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        tokensRecuperacion.remove(token);
        log.info("Contrasena restablecida para DNI {}", usuario.getDni());
    }

    /** Descarta los tokens que ya superaron su ventana de validez. */
    private void purgarTokensVencidos() {
        LocalDateTime ahora = LocalDateTime.now();
        tokensRecuperacion.entrySet().removeIf(entry -> entry.getValue().expira().isBefore(ahora));
    }
}
