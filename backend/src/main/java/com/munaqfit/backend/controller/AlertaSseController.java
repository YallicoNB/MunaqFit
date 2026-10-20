package com.munaqfit.backend.controller;

import com.munaqfit.backend.exception.CustomExceptions.UnauthorizedException;
import com.munaqfit.backend.security.JwtTokenProvider;
import com.munaqfit.backend.service.AlertaSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Alertas de stock en tiempo real (REQ-021 / REQ-031, reparto 3 - Persona 1).
 *
 * EventSource (navegador) NO puede mandar el header Authorization, asi que
 * el JWT viaja como parametro de consulta y aqui se valida a mano antes de
 * abrir el stream. Sin token valido no hay conexion: mismo criterio que el
 * filtro JWT del resto de la API.
 */
@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Notificaciones SSE de stock en tiempo real")
public class AlertaSseController {

    private final AlertaSseService alertaSseService;
    private final JwtTokenProvider jwtTokenProvider;

    public AlertaSseController(AlertaSseService alertaSseService, JwtTokenProvider jwtTokenProvider) {
        this.alertaSseService = alertaSseService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "Stream SSE de alertas de stock",
            description = "Se suscribe el frontend con EventSource. Eventos: 'stock-critico' y "
                    + "'stock-recuperado' (con el producto y el stock). Requiere JWT valido "
                    + "en el parametro ?token= porque EventSource no envia headers.")
    @GetMapping(value = "/stock", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter suscribir(@RequestParam("token") String token) {
        if (token == null || token.isBlank() || !jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Token invalido o expirado: vuelve a iniciar sesion para recibir alertas");
        }
        return alertaSseService.suscribir();
    }
}
