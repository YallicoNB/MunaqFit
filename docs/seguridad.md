# Seguridad y autorización

Punto **3.6** de la rúbrica.

## 1. Estrategia de tokens

| Token | Vida | Uso | Claim |
|:--|:--|:--|:--|
| Access | 15 min (`APP_JWT_EXPIRATION_MS`) | Header `Authorization: Bearer` | `tipo=access`, `rol`, `sub=DNI`, `jti` |
| Refresh | 7 días (`APP_JWT_REFRESH_EXPIRATION_MS`) | `POST /api/auth/refresh` | `tipo=refresh`, `rol`, `sub=DNI`, `jti` |

- El refresh se **rota** en cada uso (nuevo `jti`), y el backend rechaza un
  refresh token usado como access (`JwtAuthenticationFilter`).
- El frontend guarda ambos en `localStorage`; el `AuthInterceptor` renueva de
  forma transparente ante un `401` y reintenta una sola vez.
- Logout limpia access + refresh + datos de sesión.
- El refresh token **no** se acepta en endpoints protegidos.

## 2. Roles y permisos

| Recurso | ADMIN | EMPLEADO |
|:--|:--:|:--:|
| `/api/admin/**` (usuarios, inventario, ventas, reportes, proveedores) | ✅ | ❌ 403 |
| `/api/productos/**` (CRUD insumos) | ✅ | ❌ 403 |
| `/api/empleado/menu/**`, `/api/empleado/ventas/**`, `/api/empleado/ordenes/**` | ✅ | ✅ |
| Detalle de orden `/api/empleado/ordenes/{id}/detalle` | ✅ cualquiera | ✅ solo la propia |
| `/api/alertas/stock` | ✅ | ✅ (JWT por query) |
| `/api/get-izipay-token` | ✅ | ✅ (cobrar es acción de caja) |
| `/api/health`, `/api/auth/**`, Swagger | público | público |

## 3. Guards / middlewares

### Backend
- `JwtAuthenticationFilter`: valida el access token, rechaza refresh y carga el
  `UserDetails` en el `SecurityContext`.
- `RateLimitFilter`: 10 intentos/minuto por IP en `POST /api/auth/login`
  (responde `429` con el contrato de errores).
- `SecurityConfig`: rutas públicas, `anyRequest().authenticated()`,
  `@EnableMethodSecurity`.
- `@PreAuthorize("hasRole('ADMIN')")` en controllers administrativos.
- `RestAuthenticationEntryPoint`: `401` uniforme cuando falta/invalidó el token.
- `GlobalExceptionHandler`: `403` uniforme para `AccessDeniedException`.

### Frontend
- `authGuard`: exige sesión (access vigente o refresh válido).
- `roleGuard('ADMIN')` / `empleadoGuard`: valida el rol del payload.
- `AuthInterceptor`: agrega `Authorization`, renueva token y reintenta una vez.
- `guestGuard`: evita que un usuario logueado vuelva al login.

## 4. Otras defensas

- **Sanitización** de texto libre (`Sanitizacion`): quita HTML/JS, normaliza
  DNI/email/teléfono. Nunca toca contraseñas.
- **Bloqueo de cuenta**: 3 intentos fallidos → 5 minutos.
- **Rate limit** por IP en login.
- **CORS** restringido por `APP_CORS_ALLOWED_ORIGINS`.
- **Secretos** solo por variables de entorno; `application-local.properties`
  está en `.gitignore`.
- **Contraseñas** con BCrypt.
- **Ventas**: el empleado se toma del JWT, no del body.

## 5. Matriz de pruebas de seguridad

| Caso | Resultado esperado |
|:--|:--|
| Login correcto | `200` + access + refresh |
| Login con contraseña incorrecta | `401` |
| 3 intentos fallidos | cuenta bloqueada 5 min |
| Refresh token como access | `401` |
| Access vencido + refresh válido | `200` tras refresh automático |
| Empleado en `/api/admin/usuarios` | `403` |
| Empleado en `/api/productos` | `403` |
| Empleado consulta orden ajena | `403` |
| 11 logins/min desde una IP | `429` |
