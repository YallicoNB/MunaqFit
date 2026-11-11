package com.munaqfit.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite de intentos de login por IP (tarea 2.2).
 *
 * <p>Ventana fija en memoria: por defecto 10 peticiones por minuto y por IP.
 * Al superarla responde 429 con el mismo contrato de errores
 * {@code {timestamp, status, message, path}}. Se aplica solo a
 * {@code POST /api/auth/login}; el resto de la API no se ve afectado.</p>
 *
 * <p>Es una defensa adicional al bloqueo por cuenta que ya existe en
 * {@code AuthService}: el bloqueo frena un usuario, el rate limit frena la
 * fuerza bruta distribuida contra muchos DNI desde una misma IP.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RUTA_LOGIN = "/api/auth/login";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Ventana> ventanas = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.login.max-requests:10}")
    private int maxPeticiones;

    @Value("${app.rate-limit.login.window-seconds:60}")
    private long ventanaSegundos;

    private record Ventana(long inicio, AtomicInteger contador) {
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && RUTA_LOGIN.equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long ahora = System.currentTimeMillis();
        long duracion = ventanaSegundos * 1000L;
        String ip = obtenerIp(request);

        Ventana ventana = ventanas.compute(ip, (clave, actual) -> {
            if (actual == null || ahora - actual.inicio() >= duracion) {
                return new Ventana(ahora, new AtomicInteger(1));
            }
            actual.contador().incrementAndGet();
            return actual;
        });

        limpiarVentanasVencidas(ahora, duracion);

        if (ventana.contador().get() > maxPeticiones) {
            responderDemasiadasPeticiones(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void responderDemasiadasPeticiones(HttpServletRequest request,
                                               HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("message", "Demasiados intentos de inicio de sesion. Espera un minuto e intenta de nuevo.");
        body.put("path", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    /**
     * Detras de Render/Proxy la IP real llega en X-Forwarded-For.
     * Se toma la primera entrada, que es la del cliente.
     */
    private String obtenerIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** Evita que el mapa crezca sin limite con IPs efimeras. */
    private void limpiarVentanasVencidas(long ahora, long duracion) {
        if (ventanas.size() < 500) {
            return;
        }
        ventanas.entrySet().removeIf(entry -> ahora - entry.getValue().inicio() >= duracion);
    }
}
