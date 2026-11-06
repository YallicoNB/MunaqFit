# Contratos de API

Punto **3.5** de la rúbrica. Swagger UI local:
`http://localhost:8080/swagger-ui/index.html` · JSON: `/v3/api-docs`.

## 1. Convenciones generales

- Base URL local: `http://localhost:8080/api`.
- Autenticación: `Authorization: Bearer <access_token>`.
- Fechas: ISO-8601 (`2026-11-06T14:30:00`), fechas sin hora `YYYY-MM-DD`.
- Moneda: soles peruanos (`PEN`), `BigDecimal` con 2 decimales.
- IDs: `Long`.

## 2. Paginación y ordenamiento estándar

Request:

```http
GET /api/admin/usuarios?pagina=0&tamano=20&busqueda=ana&estado=ACTIVO&orden=nombreCompleto,asc
```

| Parámetro | Tipo | Default | Regla |
|:--|:--|:--|:--|
| `pagina` | int | `0` | base 0 |
| `tamano` | int | `20` | máximo `100` |
| `orden` | string | según endpoint | `campo,asc` o `campo,desc`; campo en lista blanca |
| `busqueda` | string | — | texto libre saneado |
| filtros | varios | — | `estado`, `categoriaId`, `fecha`, `empleadoId`, `tipo`, etc. |

Response:

```json
{
  "contenido": [],
  "pagina": 0,
  "tamano": 20,
  "totalElementos": 0,
  "totalPaginas": 0
}
```

Endpoints paginados:

| Endpoint | Campos de orden permitidos |
|:--|:--|
| `GET /api/admin/usuarios` | `fechaCreacion`, `nombreCompleto`, `dni`, `estado`, `ultimoLogin`, `rol` |
| `GET /api/admin/inventario` | `nombre`, `stockActual`, `stockMinimo`, `stockCritico`, `costoUnitario`, `ultimaReposicion`, `fechaCaducidad` |
| `GET /api/admin/ventas` | `fechaHora`, `total`, `estado`, `numeroPedido` |
| `GET /api/admin/reportes/kardex` | `fechaMovimiento`, `cantidad`, `stockAnterior`, `stockNuevo` |

> Si el campo no está en la lista blanca, se usa el orden por defecto del
> endpoint (no se devuelve error). Así se evita un 500 por una propiedad
> inexistente y no se permite ordenar por relaciones arbitrarias.

## 3. Errores

Contrato uniforme (definido en `GlobalExceptionHandler`):

```json
{
  "timestamp": "2026-11-06T14:30:00",
  "status": 400,
  "message": "El DNI debe tener exactamente 8 digitos"
}
```

| Código | Cuándo |
|:--|:--|
| `400` | Validación, JSON inválido, parámetro con tipo incorrecto |
| `401` | Sin token, token vencido, credenciales incorrectas |
| `403` | Autenticado sin rol/permiso (por ejemplo, empleado en `/api/admin/**`) |
| `404` | Recurso inexistente |
| `409` | Duplicado, stock insuficiente, venta ya pagada, bloqueo optimista |
| `429` | Rate limit del login (10 req/min por IP) |
| `500` | Error no controlado |

## 4. Endpoints principales

### Autenticación

| Método | Ruta | Body | Respuesta |
|:--|:--|:--|:--|
| POST | `/api/auth/login` | `{"dni":"12345678","password":"12345678"}` | `LoginResponse` (access + refresh) |
| POST | `/api/auth/refresh` | `{"refreshToken":"..."}` | `LoginResponse` rotado |
| POST | `/api/auth/forgot-password` | `{"email":"..."}` | 200 siempre |
| POST | `/api/auth/reset-password` | `{"token":"...","newPassword":"..."}` | 200 |
| GET | `/api/auth/validar-token` | header Bearer | `{valido,dni,rol}` |

### Admin

| Método | Ruta | Descripción |
|:--|:--|:--|
| GET | `/api/admin/usuarios` | Lista paginada + búsqueda/estado |
| POST | `/api/admin/usuarios` | Crea empleado |
| PUT | `/api/admin/usuarios/{id}` | Actualiza datos |
| PUT | `/api/admin/usuarios/{id}/rol` | Cambia rol |
| DELETE | `/api/admin/usuarios/{id}` | Inactiva (soft delete) |
| GET | `/api/admin/inventario` | Inventario paginado con DTO ampliado |
| POST | `/api/admin/inventario/reabastecer` | Suma stock + kardex |
| GET | `/api/admin/ventas` | Ventas paginadas + filtros |
| PUT | `/api/admin/ventas/{id}/estado?nuevoEstado=PAGADO` | Cambia estado |
| GET | `/api/admin/reportes/kardex` | Kardex paginado |
| GET | `/api/admin/reportes/kardex/csv` | Export CSV |
| GET | `/api/admin/reportes/ventas` | Reporte consolidado |
| GET | `/api/admin/reportes/ranking` | Ranking de bebidas |
| GET | `/api/admin/dashboard/metricas` | KPIs |
| GET | `/api/admin/proveedores` | CRUD proveedores |

### Empleado / POS

| Método | Ruta | Descripción |
|:--|:--|:--|
| GET | `/api/empleado/menu/bebidas` | Solo bebidas activas |
| GET | `/api/empleado/menu/receta/{bebidaId}` | Receta |
| POST | `/api/empleado/ventas/registrar` | Venta + detalles |
| POST | `/api/empleado/ventas/pago` | Pago |
| POST | `/api/empleado/ventas/vuelto` | Cálculo de vuelto |
| GET | `/api/empleado/ordenes/historial` | Órdenes del JWT, solo hoy |
| GET | `/api/empleado/ordenes/{id}/detalle` | Detalle (dueño o admin) |

### Integraciones

| Método | Ruta | Descripción |
|:--|:--|:--|
| GET | `/api/alertas/stock?token=JWT` | SSE de alertas de stock |
| POST | `/api/get-izipay-token` | Token de sesión Izipay |
| GET | `/api/izipay/configuracion` | ¿Izipay configurado? |
| GET | `/api/health` | Health-check público |

## 5. Códigos de estado por operación

- `GET` correcto → `200`; lista vacía → `200` con `contenido: []` o `204` en
  endpoints de menú/receta.
- `POST` de creación → `201` (productos/usuarios) o `200` (login/venta/pago).
- `PUT` → `200`.
- `DELETE` → `204` (productos) o `200` con mensaje (usuarios/proveedores).
- Errores → tabla anterior.

## 6. Ejemplos

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"dni":"12345678","password":"12345678"}'
```

Listado paginado:

```bash
curl "http://localhost:8080/api/admin/inventario?pagina=0&tamano=10&orden=stockActual,asc" \
  -H "Authorization: Bearer $TOKEN"
```

Venta con tarjeta (modo mock local):

```bash
curl -X POST http://localhost:8080/api/get-izipay-token \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"monto":18.88,"pedido":"PED-20261106-0001","descripcion":"Prueba"}'
```
