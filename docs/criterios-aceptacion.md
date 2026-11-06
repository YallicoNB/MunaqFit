# Criterios de aceptación

Mapa rápido **REQ → implementación → prueba**. Sirve para la sustentación y
para verificar que cada historia tiene un endpoint, una pantalla y un caso de
prueba.

| REQ | Historia | Backend | Frontend | Prueba |
|:--|:--|:--|:--|:--|
| REQ-001 | Login | `POST /api/auth/login` | `/login` | Postman: login 200/401 |
| REQ-002 | Logout | `POST /api/auth/logout` + limpieza local | menú de cuenta | `Token.cerrarSesion` |
| REQ-003 | Bloqueo por intentos | `AuthService` | mensaje de error | unitaria AuthService |
| REQ-004 | Recuperar contraseña | `/forgot-password`, `/reset-password` | `/forgot-password`, `/reset-password` | Postman |
| REQ-005 | Roles | `SecurityConfig` + `@PreAuthorize` | `roleGuard` | 403 en admin |
| REQ-006 | Crear empleado | `POST /api/admin/usuarios` | `/admin/usuarios` | Postman |
| REQ-007 | Listar/buscar usuarios | `GET /api/admin/usuarios` paginado | tabla + filtros | Postman paginación |
| REQ-008 | Inactivar usuario | `DELETE /api/admin/usuarios/{id}` | botón Inactivar | Postman |
| REQ-010 | Ver menú | `GET /api/empleado/menu/bebidas` | `/menu` | navegador |
| REQ-011 | Ver receta | `GET /api/empleado/menu/receta/{id}` | modal receta | Postman |
| REQ-012 | POS | `POST /api/empleado/ventas/registrar` | `/ventas` | E2E manual |
| REQ-013 | Cobrar/vuelto | `POST /api/empleado/ventas/pago` | modal cobro | unitaria PagoService |
| REQ-014 | Órdenes del empleado | `GET /api/empleado/ordenes/historial` | `/ordenes` | Postman |
| REQ-015 | Órdenes admin | `GET /api/admin/ventas` paginado | `/admin/ordenes` | Postman |
| REQ-016 | Detalle de orden | `GET /api/empleado/ordenes/{id}/detalle` | modal detalle | 403 orden ajena |
| REQ-017 | Receta en menú | `recetaDe` | modal | navegador |
| REQ-018 | Info nutricional | campos `Bebida` | `/menu` | navegador |
| REQ-019/020 | Inventario/reabastecer | `GET /api/admin/inventario`, `POST /reabastecer` | `/admin/inventario` | unitaria InventarioService |
| REQ-021 | Alertas stock | SSE `/api/alertas/stock` | toast global | conexión SSE |
| REQ-022 | Kardex | `GET /api/admin/reportes/kardex` | `/admin/kardex` | Postman |
| REQ-023/024/025 | Fidelización | `/api/empleado/fidelidad/**` | POS | Postman/unitarias |
| REQ-026 | Proveedores | CRUD `/api/admin/proveedores` | `/admin/proveedores` | Postman |
| REQ-029/030/032 | Reportes | `/api/admin/reportes/**` | `/admin/reportes` | navegador + CSV |
| REQ-031 | Stock bajo | `GET /api/admin/inventario/bajo` | alertas | Postman |
| REQ-033 | Paginación | `PageResponse` | tablas | Postman |
| REQ-034 | Rate limit | `RateLimitFilter` | — | 11 logins → 429 |
| REQ-035 | Refresh token | `/api/auth/refresh` | interceptor | unitaria Token/Interceptor |
| REQ-036 | Sanitización | `Sanitizacion` | — | unitaria |
| REQ-037 | Swagger | springdoc | — | `/v3/api-docs` |
| REQ-038 | SSE | `AlertaSseController` | `AlertaSseService` | conexión |
| REQ-039 | Izipay | `IzipayController/Service` | modal tarjeta | mock local |

## Criterio de "terminado"

- [x] Compila backend y frontend.
- [x] Migraciones Flyway aplicadas.
- [x] Endpoint documentado en Swagger.
- [x] Pantalla con estados cargando/error/vacío.
- [x] Validación en cliente y servidor.
- [x] Prueba manual o automatizada asociada.
- [x] Sin secretos en el repositorio.
