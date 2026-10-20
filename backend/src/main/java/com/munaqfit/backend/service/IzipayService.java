package com.munaqfit.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munaqfit.backend.dto.IzipayTokenRequest;
import com.munaqfit.backend.dto.IzipayTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Integracion de pagos con Izipay (fase 1: sandbox).
 *
 * Flujo oficial de Izipay "Payment Form embebido":
 *   1. El frontend le pide a NUESTRO backend un token de sesion.
 *   2. Este servicio llama a la API REST de Izipay (Crear Token de sesion)
 *      con las credenciales del comercio, que viven solo en variables de
 *      entorno: jamas llegan al navegador.
 *   3. El backend devuelve al frontend el tokenGUID + la llave RSA publica.
 *   4. El frontend carga el SDK y hace LoadForm(token, keyRSA); al terminar
 *      trae un callbackResponse cuyo Code "00" significa aprobado.
 *
 * La URL, el merchantCode y las llaves se leen de variables de entorno
 * (IZIPAY_*), asi que pasar de sandbox a produccion NO exige tocar codigo:
 * solo cambiar las variables en Render (tarea 1.1/1.5 del reparto 3).
 */
@Service
public class IzipayService {

    private static final Logger log = LoggerFactory.getLogger(IzipayService.class);

    /** Codigo de respuesta de exito de la API de token de Izipay. */
    private static final String CODIGO_EXITO = "0000";

    @Value("${app.izipay.token-url}")
    private String tokenUrl;

    @Value("${app.izipay.script-url}")
    private String scriptUrl;

    @Value("${app.izipay.merchant-code}")
    private String merchantCode;

    @Value("${app.izipay.api-user}")
    private String apiUser;

    @Value("${app.izipay.api-password}")
    private String apiPassword;

    @Value("${app.izipay.key-rsa}")
    private String keyRsa;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Timeouts cortos: si Izipay no responde, el empleado no debe quedar
     *  colgado esperando; mejor un error claro y reintento. */
    private final RestClient restClient = RestClient.builder()
            .requestFactory(crearRequestFactory())
            .build();

    private static SimpleClientHttpRequestFactory crearRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8_000);
        factory.setReadTimeout(15_000);
        return factory;
    }

    /** true si ya llegaron las credenciales del comercio (sandbox o reales). */
    public boolean estaConfigurado() {
        return !isBlank(tokenUrl) && !isBlank(merchantCode)
                && !isBlank(apiPassword) && !isBlank(keyRsa);
    }

    /**
     * Pide a Izipay el token de sesion que el frontend usara en LoadForm.
     * Lanza IllegalStateException con mensaje en espanol si el proveedor
     * responde un codigo distinto de exito; el GlobalExceptionHandler lo
     * convierte en un error legible para el empleado.
     */
    public IzipayTokenResponse crearTokenDeSesion(IzipayTokenRequest request) {

        if (!estaConfigurado()) {
            throw new IllegalStateException(
                    "Izipay no esta configurado: faltan las credenciales del comercio "
                            + "(variables IZIPAY_MERCHANT_CODE / IZIPAY_API_PASSWORD / IZIPAY_KEY_RSA). "
                            + "Cobra en efectivo o digital mientras se completan.");
        }
        if (request == null || request.getMonto() == null
                || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del cobro debe ser mayor a cero");
        }
        if (isBlank(request.getPedido())) {
            throw new IllegalArgumentException("El numero de pedido es obligatorio");
        }

        // Cuerpo del llamado "Crear Token de sesion". Moneda siempre PEN.
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("moneda", "PEN");
        cuerpo.put("monto", request.getMonto().setScale(2).toPlainString());
        cuerpo.put("indicador", "1");
        cuerpo.put("descripcion",
                !isBlank(request.getDescripcion()) ? request.getDescripcion() : "MunaqFit " + request.getPedido());
        cuerpo.put("pedido", request.getPedido());

        JsonNode respuesta;
        try {
            respuesta = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", apiPassword)
                    .header("user", apiUser != null ? apiUser : merchantCode)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException e) {
            log.error("Izipay no respondio al pedir el token de sesion: {}", e.getMessage());
            throw new IllegalStateException("Izipay no responde en este momento. Intenta de nuevo o cobra en efectivo.");
        }

        if (respuesta == null) {
            throw new IllegalStateException("Izipay devolvio una respuesta vacia. Intenta de nuevo.");
        }

        String codigo = texto(respuesta, "codeResponse", "code", "Code");
        String tokenGuid = texto(respuesta, "tokenGUID", "tokenGuid", "authorization");

        if (!CODIGO_EXITO.equals(codigo) || isBlank(tokenGuid)) {
            String detalle = texto(respuesta, "description", "message", "Message");
            log.warn("Izipay rechazo el token de sesion. Codigo: {}, detalle: {}", codigo, detalle);
            throw new IllegalStateException("Izipay no genero el token"
                    + (!isBlank(detalle) ? ": " + detalle : ". Verifica las credenciales del comercio."));
        }

        return new IzipayTokenResponse(
                tokenGuid,
                keyRsa,
                merchantCode,
                scriptUrl,
                "PEN",
                request.getMonto().setScale(2).toPlainString(),
                request.getPedido());
    }

    private static boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    /** Lee el primer campo existente entre los nombres candidatos. */
    private static String texto(JsonNode nodo, String... campos) {
        for (String campo : campos) {
            JsonNode valor = nodo.get(campo);
            if (valor != null && !valor.isNull() && !valor.asText().isBlank()) {
                return valor.asText();
            }
        }
        return null;
    }
}
