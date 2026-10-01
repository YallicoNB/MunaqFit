# Guia de deploy: MunaqFit (Render + Vercel + MySQL gratis)

Arquitectura:

```
Frontend  ->  Vercel   (Angular estatico)   ->  https://munaqfit.vercel.app
Backend   ->  Render   (Spring Boot + Docker) ->  https://munaqfit-backend.onrender.com
Base BD   ->  TiDB Cloud Serverless (MySQL gratis)
Swagger   ->  https://munaqfit-backend.onrender.com/swagger-ui/index.html
```

---

## 1. Crear la cuenta compartida del equipo

1. Crea un Gmail nuevo del proyecto (por ejemplo `munaqfitproyecto@gmail.com`).
2. Todos los del equipo entran con ese correo a Render, Vercel y TiDB.

> En Vercel el plan Hobby es gratis pero es de **un solo miembro**, asi que
> conviene compartir la misma cuenta en vez de invitar a cada compañero.

---

## 2. Crear la base de datos MySQL gratis (TiDB Cloud)

1. Entra en <https://www.tidbcloud.com> e inicia sesion con el Gmail del proyecto.
2. Crea un **Serverless Tier** en la region mas cercana.
3. Anota estos tres datos (los pide TiDB al crear el cluster):
   - **Host / endpoint** (parecido a `gateway01.xxxx.prod.aws.tidbcloud.com`)
   - **User**
   - **Password**
4. Concede permisos a tu IP o deja el cluster publico para el proyecto (mas facil para deploy).

### Construir la JDBC URL

Si el host es `gateway01.prod.region.aws.tidbcloud.com`, TiDB normalmente **no** da puerto explicito; el driver usa el **4000** para MySQL. La JDBC URL queda:

```
jdbc:mysql://GATEWAY_HOST:4000/munaqfit?useSSL=true&serverTimezone=UTC
```

Si TiDB te muestra un puerto especifico, usa ese:

```
jdbc:mysql://GATEWAY_HOST:PUERTO/munaqfit?useSSL=true&serverTimezone=UTC
```

> Tip: puedes probar la conexion desde Workbench o DBeaver antes de tocar el codigo.

### Cargar el esquema y los datos

El backend usa **Flyway**, asi que las tablas se crean solas al arrancar. Pero los datos de prueba se cargan aparte.

Despues del primer deploy del backend:

```bash
mysql -h GATEWAY_HOST -P 4000 -u USER -p munaqfit < database/seed.sql
```

---

## 3. Variables de entorno del backend (en Render)

Crea un **Web Service** en <https://render.com> conectado al repo `MunaqFit`.

**Opciones del servicio:**
- Environment: `Docker`
- Region: la misma que tu BD (o la mas cercana)
- Instance type: `Free`

**Variables de entorno (Settings -> Environment):**

| Variable | Valor |
|:--|:--|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://GATEWAY_HOST:4000/munaqfit?useSSL=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | tu user de TiDB |
| `SPRING_DATASOURCE_PASSWORD` | tu password de TiDB |
| `APP_JWT_SECRET` | una cadena larga y aleatoria (min. 32 chars) |
| `APP_CORS_ALLOWED_ORIGINS` | `https://munaqfit.vercel.app` |
| `SWAGGER_ENABLED` | `true` (pon `false` si no quieres que se vea en linea) |
| `SPRING_JPA_SHOW_SQL` | `false` (en produccion no hace falta ver el SQL) |

> El puerto lo pone Render solo con `PORT`. No lo definas a mano.

**Health check path:** `/v3/api-docs` (o `/api/auth/validar-token`)

Cuando Render termine, te da una URL tipo `https://munaqfit-backend.onrender.com`.
**Copia esa URL** para el siguiente paso.

---

## 4. Frontend en Vercel

### 4.1 Ajustar la URL del backend

Abre `frontend/src/environments/environment.prod.ts` y pon la URL real de Render:

```ts
export const environment = {
  production: true,
  apiUrl: 'https://munaqfit-backend.onrender.com/api',
};
```

Sube el cambio:

```bash
git add .
git commit -m "chore(frontend): apunta al backend desplegado en Render"
git push
```

### 4.2 Importar el proyecto

1. Entra en <https://vercel.com> con el Gmail del proyecto.
2. **Add New -> Project** e importa `YallicoNB/MunaqFit`.
3. En **Root Directory** pon `frontend`.
4. Vercel detecta Angular (Framework Preset: Angular). deberia usar:
   - Build Command: `npm run build`
   - Output Directory: `dist/frontend/browser`
5. Clic en **Deploy**.

> El `vercel.json` ya tiene el rewrite SPA, asi que rutas como `/dashboard`
> o `/admin/usuarios` funcionaran al recargar la pagina.

Cuando termine, Vercel te da `https://munaqfit.vercel.app`.

---

## 5. Cerrar el circulo: CORS

Si el frontend ya esta desplegado y cambiaste la URL de Render despues:

1. Actualiza `APP_CORS_ALLOWED_ORIGINS` en Render con el dominio final de Vercel.
2. Reinicia el servicio en Render.

Si Vercel te dio un dominio tipo `munaqfit-abc123.vercel.app`, puedes incluir ambos separados por coma:

```
https://munaqfit-abc123.vercel.app,https://munaqfit.vercel.app
```

---

## 6. Verificar que todo funciona en linea

| Prueba | URL | Resultado esperado |
|:--|:--|:--|
| Frontend | `https://munaqfit.vercel.app` | Login visible |
| Backend vivo | `https://munaqfit-backend.onrender.com/v3/api-docs` | JSON de OpenAPI |
| Swagger UI | `https://munaqfit-backend.onrender.com/swagger-ui/index.html` | Documentacion navegable |
| Login desde la UI | `POST /api/auth/login` con `{ "dni": "12345678", "password": "12345678" }` | 200 + token |
| Endpoint protegido | `GET /api/admin/dashboard/metricas` con token | 200 + metricas |

> **Render free se duerme** tras unos minutos de inactividad. La primera carga
> puede tardar ~30-60 s mientras el contenedor despierta. Es normal.

---

## 7. Volver a local

`application.properties` y los `environment.ts` tienen valores por defecto para
local, asi que para trabajar en tu maquina solo levantas:

```bash
cd backend && mvn spring-boot:run    # MySQL local en 3306
cd frontend && npm start             # http://localhost:4200
```

Los `environment.ts` siguen apuntando a `localhost:8080` y el CORS solo permite
`localhost:4200`. No se toco nada del flujo local.

---

## 8. Problemas frecuentes del deploy

| Síntoma | Causa probable | Solución |
|:--|:--|:--|
| Render no encuentra el puerto | Falta exponer `PORT` | El Dockerfile ya usa `-Dserver.port=$PORT` |
| `403` o error de CORS en el navegador | `APP_CORS_ALLOWED_ORIGINS` no coincide con el dominio de Vercel | Agrega el dominio exacto |
| La app carga pero "no se conecta" | `environment.prod.ts` tiene la URL vieja de Render | Actualizala y vuelve a desplegar Vercel |
| Vercel da 404 al recargar `/dashboard` | Falta rewrite SPA | Ya esta en `vercel.json` |
| La base no conecta | JDBC URL mal (puerto/SSL) | Revisa el host y el puerto de TiDB |
| La primera carga tarda mucho | Render free se despierta | Espera, es normal |
