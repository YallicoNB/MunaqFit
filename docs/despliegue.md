# Despliegue e infraestructura

Punto **3.10** de la rúbrica.

## 1. Entornos

| Entorno | Perfil | Base de datos | Frontend | Backend |
|:--|:--|:--|:--|:--|
| Local | `local` | `munaqfit_dev` (TiDB) o MySQL local | `ng serve` :4200 | `mvn spring-boot:run` :8080 |
| Producción | `prod` | `munaqfit` en TiDB Cloud (`us-west-2`) | <https://munaq-fit-rho.vercel.app> | <https://munaqfit-backend.onrender.com> |

El perfil se elige con `SPRING_PROFILES_ACTIVE=local|prod`.
Las credenciales locales van en `application-local.properties`, que está en
`.gitignore`. En producción los secretos se cargan como variables de entorno en
Render.

## 2. Variables de entorno

| Variable | Uso |
|:--|:--|
| `SPRING_DATASOURCE_URL` | JDBC de TiDB/MySQL (con TLS) |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | credenciales de la base |
| `APP_JWT_SECRET` | firma JWT (mín. 32 caracteres; Render lo genera) |
| `APP_JWT_EXPIRATION_MS` | access token (900000 = 15 min) |
| `APP_JWT_REFRESH_EXPIRATION_MS` | refresh token (604800000 = 7 días) |
| `APP_CORS_ALLOWED_ORIGINS` | dominio del frontend |
| `APP_CORS_ALLOW_ALL` | `false` en producción |
| `APP_RATE_LIMIT_LOGIN_MAX` / `_WINDOW` | rate limit de login |
| `SWAGGER_ENABLED` | `true` local, `false` en producción |
| `SPRING_JPA_SHOW_SQL` | `false` en producción |
| `LOG_LEVEL` | `INFO` en producción |
| `IZIPAY_MOCK` | `true` en la demo pública (pago simulado); `false` al cargar credenciales sandbox |
| `IZIPAY_*` | credenciales sandbox/producción, solo si `IZIPAY_MOCK=false` |
| `PORT` | lo inyecta Render |

## 3. Despliegue actual (Render + Vercel + TiDB)

### Base de datos (TiDB Cloud)

- Cluster Serverless **`munaqfit`**, región **Oregon (`us-west-2`)**.
- Host `gateway01.us-west-2.prod.aws.tidbcloud.com`, puerto `4000`, TLS
  obligatorio.
- La base se crea vacía. Flyway aplica las 11 migraciones en el primer
  arranque del backend y deja el sistema funcional: usuarios demo, 21 bebidas,
  25 insumos, 98 recetas e IGV `0.18`.
- **No ejecutar `database/seed.sql` en producción**: ese script hace `TRUNCATE`
  y borraría los datos creados por las migraciones.

### Backend (Render)

- Blueprint **`munaqfit-produccion`**, definido en `render.yaml`.
- Servicio **`munaqfit-backend`**, runtime Docker, plan Free, región Oregon.
- URL pública: <https://munaqfit-backend.onrender.com>.
- Health check: <https://munaqfit-backend.onrender.com/api/health>.
- Variables configuradas en el Blueprint: URL/usuario/contraseña de TiDB,
  `APP_CORS_ALLOWED_ORIGINS=https://munaq-fit-rho.vercel.app`,
  `SPRING_PROFILES_ACTIVE=prod`, `IZIPAY_MOCK=true`, JWT generado y límites de
  login.
- `render.yaml` es la fuente de verdad: los cambios en `main` se sincronizan
  automáticamente con el Blueprint.

### Frontend (Vercel)

- Proyecto **`munaq-fit`**, importado desde `YallicoNB/MunaqFit`, rama `main`.
- Root Directory: `frontend`. Preset: Angular.
- Dominio de producción: <https://munaq-fit-rho.vercel.app>.
- `frontend/vercel.json` contiene el rewrite SPA.
- `frontend/src/environments/environment.prod.ts` apunta a
  `https://munaqfit-backend.onrender.com/api`.

### Verificación realizada

```bash
curl https://munaqfit-backend.onrender.com/api/health
curl -I https://munaq-fit-rho.vercel.app
```

Resultados comprobados el **2026-11-10**:

- `/api/health` → `{"status":"UP"}`.
- Login admin `12345678` / `12345678` → token y rol `ADMIN`.
- Catálogo del POS → **21 bebidas activas**.
- CORS desde `https://munaq-fit-rho.vercel.app` → `200` y
  `Access-Control-Allow-Origin` correcto.
- Venta de prueba en el POS con **TARJETA** en modo simulado → pedido
  `PED-20261111-0001` por **S/ 18.88**.

## 4. CI/CD

Workflow: `.github/workflows/ci.yml`.

- **Backend:** JDK 17 + MySQL 8 como service, `mvn -B verify` (compila, tests y
  JaCoCo).
- **Frontend:** Node 20, `npm ci`, `npm run build`, `npm run test:ci`
  (Karma/ChromeHeadless) y cobertura.
- **Deploy:** Render y Vercel despliegan automáticamente al hacer push a
  `main`.

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
- `SWAGGER_ENABLED=false` para no exponer la documentación en la demo pública.
- `IZIPAY_MOCK=true`: el flujo de tarjeta se simula. Al recibir credenciales
  sandbox, cambiar a `false` y agregar las variables `IZIPAY_*` en Render.
- Render Free duerme tras inactividad; la primera petición puede tardar ~50 s.
  `/api/health` permite mantenerlo despierto con un cron de ping.
- No ejecutar `database/seed.sql` sobre la base de producción.
