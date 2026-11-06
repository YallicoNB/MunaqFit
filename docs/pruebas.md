# Pruebas y calidad

Punto **3.12** de la rúbrica.

## 1. Estrategia

| Nivel | Backend | Frontend |
|:--|:--|:--|
| Unitarias | JUnit 5 + Mockito (servicios y utilidades) | Jasmine + Karma (servicios, guards, interceptor, componentes) |
| Integración | `@SpringBootTest` + MockMvc + MySQL 8 (CI) | TestBed + `HttpClientTestingModule` |
| E2E / contrato | Colección Postman | — |
| Cobertura | JaCoCo (`target/site/jacoco`) | Karma coverage (`coverage/`) |

## 2. Comandos

```bash
# Backend
cd backend
mvn test                 # unitarias + integración local
mvn verify               # tests + JaCoCo

# Frontend
cd frontend
npm run test             # modo watch
npm run test:ci          # una pasada + ChromeHeadless
npm run test:coverage    # cobertura
```

> **Windows:** si Chrome falla al iniciar Karma con
> `Invalid file descriptor to ICU data`, apunta Karma a Edge (Chromium):
>
> ```powershell
> $env:CHROME_BIN='C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'
> npm run test:ci
> ```

## 3. Casos mínimos

### Resultado actual

- Backend: **32 pruebas**, 0 fallos (`mvn test` / `mvn verify`).
- Frontend: **21 pruebas**, 0 fallos (`npm run test:ci`).
- Cobertura JaCoCo: `backend/target/site/jacoco/index.html`.
- Cobertura Karma: `frontend/coverage/frontend/index.html`.
- Postman: `postman/MunaqFit.postman_collection.json`.

### Backend
- `JwtTokenProvider`: genera access/refresh, detecta el tipo, rechaza expirados.
- `AuthService`: login correcto, contraseña incorrecta, cuenta inactiva,
  bloqueo por intentos, refresh válido/inválido.
- `Sanitizacion`: quita HTML/JS, normaliza DNI/email/teléfono.
- `PagoService`: efectivo con vuelto, digital sin vuelto, venta ya pagada.
- `InventarioService`: reabastecimiento suma stock, registra kardex y actualiza
  `ultimaReposicion`.
- `ProductoService`: crea/actualiza con proveedores y sanitiza el nombre.
- `PageResponse` / `Ordenamiento`: contrato y whitelist.

### Frontend
- `Token`: guarda/limpia sesión, access vencido + refresh válido.
- `AuthInterceptor`: 401 → refresh → reintento; refresh fallido → logout.
- `authGuard` / `roleGuard`: sesión y roles.
- `Card`: muestra `imagenUrl`, fallback sin foto y fallback por error.
- `Login`: formulario inválido no llama al servicio; válido sí.

## 4. Postman

Colección: `postman/MunaqFit.postman_collection.json`.

Variables:
- `baseUrl` = `http://localhost:8080`
- `token`, `refreshToken`, `empleadoToken`

Carpetas:
1. Auth (login, refresh, validar-token)
2. Admin (usuarios paginado, inventario, ventas, kardex)
3. Empleado (menú, recetas, historial)
4. Seguridad (403 empleado en admin, refresh como access → 401)
5. Integraciones (health, SSE no testeable en Postman, Izipay token)

## 5. CI

`.github/workflows/ci.yml` ejecuta en cada push/PR:
- Backend con MySQL 8 service: `mvn -B verify`.
- Frontend: `npm ci`, `npm run build`, `npm run test:ci`.
- Publica reportes de cobertura como artefactos.

## 6. Criterios de aceptación

Ver `docs/criterios-aceptacion.md` (mapa REQ → endpoint/UI/prueba).
