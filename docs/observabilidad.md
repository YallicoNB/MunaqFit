# Observabilidad

Punto **3.11** de la rúbrica.

## 1. Health-checks

| Endpoint | Público | Uso |
|:--|:--:|:--|
| `GET /api/health` | Sí | health-check de Render / cron anti-sleep |
| `GET /actuator/health` | Sí | health con detalle de BD (cuando Actuator está activo) |
| `GET /actuator/info` | Sí | información de la app |
| `GET /actuator/metrics` | Autenticado | métricas JVM/HTTP |

`/api/health` es intencionalmente liviano: no consulta la base para no
consumir conexiones de TiDB con un cron. El health de Actuator sí incluye el
`DataSourceHealthIndicator` (verifica que la conexión JDBC responda).

## 2. Logs

- SLF4J + Logback en todos los servicios clave.
- Eventos registrados: login exitoso/fallido, refresh, venta registrada, pago,
  reabastecimiento, CRUD de productos, visitas de fidelidad y errores
  controlados/no controlados.
- En `prod` se activa **structured logging** (ECS) con
  `logging.structured.format.console=ecs`, de modo que Render puede indexar
  los campos `timestamp`, `level`, `logger`, `message`, `traceId`, etc.
- En local se mantiene el formato legible por consola.

## 3. Métricas

Con Actuator + Micrometer:

| Métrica | Para qué |
|:--|:--|
| `http.server.requests` | latencia, códigos, endpoints lentos |
| `jvm.memory.used` | memoria del contenedor |
| `jdbc.connections.active` | pool de conexiones |
| `process.uptime` | uptime |

Se exponen en `/actuator/metrics` y `/actuator/prometheus` (si se agrega el
registry de Prometheus).

## 4. Dashboard

Opciones sin costo adicional:

1. **Render Dashboard → Logs**: búsqueda por `level:ERROR`, `logger`, etc.
2. **Render Metrics**: CPU/memoria/requests del servicio.
3. **UptimeRobot / cron-job.org**: ping a `/api/health` cada 10 min para evitar
   que Render Free se duerma y alertar si responde distinto de 200.
4. **Grafana Cloud free** (opcional): scrape de `/actuator/prometheus`.

## 5. Qué revisar en una incidencia

1. `GET /api/health` → ¿la app está arriba?
2. `GET /actuator/health` → ¿la BD responde?
3. Logs de Render filtrados por `ERROR` y por el `traceId` de la petición.
4. `http.server.requests` para ver si el error es 4xx (cliente) o 5xx (servidor).
5. Confirmar que `IZIPAY_MOCK` y `SWAGGER_ENABLED` tienen el valor del entorno.
