package com.munaqfit.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.munaqfit.backend.model.Producto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Tiempo real (REQ-021 / REQ-031, reparto 3 - Persona 1).
 *
 * Mantiene una conexion SSE abierta por cada sesion autenticada y les avisa
 * al instante cuando un insumo cae a stock critico o cuando se recupera.
 * El frontend se suscribe a GET /api/alertas/stock y muestra el toast.
 *
 * Por que SSE y no WebSocket: el proyecto es HTTP puro en Render (plan
 * gratuito, sin WS persistente), SSE se apoya en la conexion HTTP que ya
 * existe y EventSource del navegador reconecta solo. Suficiente para avisos.
 */
@Service
public class AlertaSseService {

    private static final Logger log = LoggerFactory.getLogger(AlertaSseService.class);

    /** Segundos entre pings keep-alive: Render corta conexiones quietas (~55s). */
    private static final int SEGUNDOS_PING = 25;

    /** Emisores vivos. CopyOnWrite: se itera mientras otros hilos agregan/quitan. */
    private final List<SseEmitter> emisores = new CopyOnWriteArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Hilo unico que mantiene vivas las conexiones con pings. */
    private final ScheduledExecutorService keepAlive =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread hilo = new Thread(runnable, "sse-keep-alive");
                hilo.setDaemon(true);
                return hilo;
            });

    @PostConstruct
    void iniciarKeepAlive() {
        keepAlive.scheduleAtFixedRate(this::enviarPing, SEGUNDOS_PING, SEGUNDOS_PING, TimeUnit.SECONDS);
    }

    @PreDestroy
    void detener() {
        keepAlive.shutdownNow();
        emisores.forEach(SseEmitter::complete);
        emisores.clear();
    }

    /** Crea y registra el emisor de una sesion. */
    public SseEmitter suscribir() {
        // Sin timeout fijo: la vida de la conexion la da el ping.
        SseEmitter emisor = new SseEmitter(0L);
        emisores.add(emisor);
        emisor.onCompletion(() -> quitar(emisor));
        emisor.onTimeout(() -> quitar(emisor));
        emisor.onError(e -> quitar(emisor));

        enviar(emisor, "conectado", Map.of("mensaje", "Alertas de stock activas"));
        log.info("SSE de stock conectado. Clientes activos: {}", emisores.size());
        return emisor;
    }

    /**
     * Avisa a todos los conectados que un insumo llego a stock critico.
     * Debe llamarse con el stock ya actualizado en el objeto producto.
     */
    public void notificarStockCritico(Producto producto, BigDecimal stockNuevo) {
        Map<String, Object> datos = base(producto, stockNuevo);
        datos.put("tipo", "CRITICO");
        datos.put("mensaje", "'" + producto.getNombre() + "' llego a stock critico ("
                + formatear(stockNuevo) + " " + unidad(producto) + ")");
        broadcast("stock-critico", datos);
    }

    /** Avisa que un insumo salio del estado critico (tras reabastecer). */
    public void notificarStockRecuperado(Producto producto, BigDecimal stockNuevo) {
        Map<String, Object> datos = base(producto, stockNuevo);
        datos.put("tipo", "RECUPERADO");
        datos.put("mensaje", "'" + producto.getNombre() + "' salio del stock critico ("
                + formatear(stockNuevo) + " " + unidad(producto) + ")");
        broadcast("stock-recuperado", datos);
    }

    /**
     * Ejecuta la notificacion DESPUES del commit de la transaccion actual.
     * Si la transaccion revienta, no se avisa: una alerta falsa ensucia la
     * demo y hace que el equipo deje de confiar en los avisos.
     */
    public void notificarTrasCommit(Runnable notificacion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificacion.run();
                }
            });
        } else {
            notificacion.run();
        }
    }

    // ===================== Internos =====================

    private Map<String, Object> base(Producto producto, BigDecimal stockNuevo) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("productoId", producto.getId());
        datos.put("nombre", producto.getNombre());
        datos.put("stockActual", stockNuevo != null ? stockNuevo.doubleValue() : null);
        datos.put("stockCritico", producto.getStockCritico() != null
                ? producto.getStockCritico().doubleValue() : null);
        datos.put("unidad", unidad(producto));
        return datos;
    }

    private void broadcast(String evento, Map<String, Object> datos) {
        if (emisores.isEmpty()) {
            return;
        }
        log.info("SSE -> {} a {} cliente(s)", evento, emisores.size());
        for (SseEmitter emisor : emisores) {
            enviar(emisor, evento, datos);
        }
    }

    private void enviar(SseEmitter emisor, String evento, Map<String, Object> datos) {
        try {
            String json = objectMapper.writeValueAsString(datos);
            emisor.send(SseEmitter.event().name(evento).data(json));
        } catch (Exception e) {
            // El cliente ya se fue: se limpia y listo, no es un error del
            // sistema. Se captura Exception y no solo IOException porque
            // Spring lanza AsyncRequestNotUsableException (ServletException)
            // cuando la respuesta murio, y eso romperia el hilo.
            quitar(emisor);
        }
    }

    private void enviarPing() {
        for (SseEmitter emisor : emisores) {
            try {
                emisor.send(SseEmitter.event().comment("ping"));
            } catch (Exception e) {
                quitar(emisor);
            }
        }
    }

    private void quitar(SseEmitter emisor) {
        if (emisores.remove(emisor)) {
            try {
                emisor.complete();
            } catch (Exception ignorado) {
                // Ya estaba cerrado del otro lado.
            }
            log.debug("SSE desconectado. Clientes activos: {}", emisores.size());
        }
    }

    private static String unidad(Producto producto) {
        return producto.getUnidadMedida() != null ? producto.getUnidadMedida().name() : "";
    }

    private static String formatear(BigDecimal valor) {
        return valor != null ? valor.stripTrailingZeros().toPlainString() : "0";
    }
}
