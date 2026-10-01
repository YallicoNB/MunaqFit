package com.munaqfit.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Sonda de disponibilidad para el plan gratuito de Render.
 *
 * Render duerme una instancia Free tras 15 minutos sin trafico entrante y
 * tarda cerca de un minuto en volver a levantarse. Un cron que llame a este
 * endpoint cada 10 minutos mantiene la instancia despierta.
 *
 * No consulta la base de datos a proposito: Render puede suspender servicios
 * Free que generan volumen inusual de trafico hacia bases de datos externas
 * (en este proyecto TiDB), y este ping no debe contabilizar como tal. Tampoco
 * necesita token, para que el cron no tenga que guardar credenciales.
 *
 * Ojo: no usar /robots.txt para esto. Render responde ahi con un "disallow
 * all" automatico mientras la instancia duerme, y esa peticion no la despierta.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}