# Despliegue e infraestructura

Punto **3.10** de la rúbrica.

## 1. Entornos

| Entorno | Perfil | Base de datos | Frontend | Backend |
|:--|:--|:--|:--|:--|
| Local | `local` | `munaqfit_dev` (TiDB) o MySQL local | `ng serve` :4200 | `mvn spring-boot:run` :8080 |
| Staging | `staging` | TiDB staging | Vercel preview | Render staging |
| Producción | `prod` | TiDB producción | Vercel | Render |

El perfil se elige con `SPRING_PROFILES_ACTIVE=local|staging|prod`.
Los perfiles `staging` y `prod` viven en
`backend/src/main/resources/application-staging.properties` y
`application-prod.properties`; los secretos siempre por variables de entorno.

## 2. Variables de entorno

Copia `.env.example` a tu gestor de secretos (Render/Vercel) y completa:

| Variable | Uso |
|:--|:--|
| `SPRING_DATASOURCE_URL` | JDBC de TiDB/MySQL |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | credenciales |
| `APP_JWT_SECRET` | firma JWT (mín. 32 chars) |
| `APP_JWT_EXPIRATION_MS` | access (900000 = 15 min) |
| `APP_JWT_REFRESH_EXPIRATION_MS` | refresh (604800000 = 7 días) |
| `APP_CORS_ALLOWED_ORIGINS` | dominio del frontend |
| `APP_CORS_ALLOW_ALL` | `false` en producción |
| `APP_RATE_LIMIT_LOGIN_MAX` / `_WINDOW` | rate limit login |
| `SWAGGER_ENABLED` | `true` local/staging, `false` prod |
| `SPRING_JPA_SHOW_SQL` | `false` en prod |
| `LOG_LEVEL` | `INFO` en prod |
| `IZIPAY_*` | credenciales sandbox/producción |
| `IZIPAY_MOCK` | `true` solo local |
| `PORT` | lo inyecta Render |

## 3. Despliegue con Render + Vercel

### Backend (Render)
1. Crear un **Web Service** tipo **Docker** apuntando a `backend/`.
2. Render usa `backend/Dockerfile` y el puerto de `$PORT`.
3. Configurar las variables de la tabla.
4. Health check: `/api/health`.
5. Opcional: usar `render.yaml` de la raíz para crear el servicio como
   infraestructura como código.

### Frontend (Vercel)
1. Importar el repo y poner **Root Directory** = `frontend`.
2. Build: `npm run build`; Output: `dist/frontend/browser`.
3. `frontend/vercel.json` ya tiene el rewrite SPA.
4. Configurar `environment.prod.ts` con la URL real del backend.

### Verificación
```bash
curl https://TU-BACKEND.onrender.com/api/health
curl https://TU-BACKEND.onrender.com/v3/api-docs
curl -I https://TU-FRONTEND.vercel.app
```

> **Estado actual:** las URLs antiguas (`munaqfit.onrender.com`,
> `munaqfit-backend.onrender.com`, `munaqfit.vercel.app`) devuelven 404 o no
> responden. Hay que redeployar o actualizar `environment.prod.ts` y
> `deploy.md` con la URL vigente. Es el único punto que depende de tus
> credenciales de Render/Vercel.

## 4. CI/CD

Workflow: `.github/workflows/ci.yml`.

- **Backend:** JDK 17 + MySQL 8 como service, `mvn -B verify` (compila, tests y
  JaCoCo).
- **Frontend:** Node 20, `npm ci`, `npm run build`, `npm run test:ci`
  (Karma/ChromeHeadless) y cobertura.
- **Deploy:** Render y Vercel pueden desplegar automáticamente al hacer push a
  `main`. Si prefieres deploy manual, agrega los secrets
  `RENDER_DEPLOY_HOOK` y `VERCEL_DEPLOY_HOOK` y un job `workflow_dispatch`.

Comandos locales equivalentes:

```bash
# backend
cd backend && mvn -B verify

# frontend
cd frontend && npm ci && npm run build && npm run test:ci
```

## 5. Notas de producción

- `spring.jpa.hibernate.ddl-auto=validate`: Flyway es la única fuente de
  esquema.
- `SWAGGER_ENABLED=false` si no quieres exponer la documentación.
- `IZIPAY_MOCK=false`: el flujo real requiere credenciales sandbox.
- Render Free duerme tras inactividad; `/api/health` permite un cron de ping.
