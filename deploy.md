# Guía de deploy: MunaqFit (Render + Vercel + TiDB Cloud)

Arquitectura vigente:

```
Frontend  ->  Vercel   (Angular estático)     ->  https://munaq-fit-rho.vercel.app
Backend   ->  Render   (Spring Boot + Docker) ->  https://munaqfit-backend.onrender.com
Base BD   ->  TiDB Cloud Serverless (MySQL)   ->  munaqfit (us-west-2)
```

> Esta guía describe el despliegue público del proyecto. No incluye contraseñas
> ni cadenas de conexión reales: esos valores viven en Render y en
> `application-local.properties` (archivo ignorado por Git).

---

## 1. Cuentas del proyecto

- **GitHub:** `YallicoNB` — repositorio público `YallicoNB/MunaqFit`.
- **Render, Vercel y TiDB Cloud:** se accede con GitHub OAuth usando la cuenta
  `YallicoNB`. El correo de contacto del proyecto es `yalliconoe@gmail.com`.
- No se reutilizan servicios de cuentas anteriores: Render, Vercel y la base de
  datos se crearon desde cero para esta entrega.

---

## 2. Base de datos (TiDB Cloud)

1. Entra en <https://tidbcloud.com> con GitHub.
2. Crea un cluster **Serverless** llamado `munaqfit` en la región **Oregon
   (`us-west-2`)**.
3. En **Connect** copia:
   - Host: `gateway01.us-west-2.prod.aws.tidbcloud.com`
   - Puerto: `4000`
   - Usuario y contraseña del cluster
   - Certificado TLS (TiDB lo exige)
4. Crea la base de datos (una sola vez):

   ```sql
   CREATE DATABASE IF NOT EXISTS munaqfit;
   ```

   No uses la base `sys`: es del sistema.

### JDBC URL

```text
jdbc:mysql://gateway01.us-west-2.prod.aws.tidbcloud.com:4000/munaqfit?useSSL=true&requireSSL=true&serverTimezone=America/Lima&allowPublicKeyRetrieval=true
```

### Esquema y datos

Flyway se ejecuta al arrancar el backend y aplica las migraciones `V1`–`V16`.
Una base vacía queda lista con:

- 4 usuarios demo.
- 21 bebidas activas del catálogo oficial.
- 25 insumos y 98 recetas.
- IGV `0.18`.

> **No ejecutes `database/seed.sql` en producción.** Ese archivo hace
> `TRUNCATE` y borraría los datos de las migraciones.

---

## 3. Backend en Render

1. En Render: **New → Blueprint**.
2. Selecciona el repo `YallicoNB/MunaqFit`, rama `main` y `render.yaml`.
3. Nombre del Blueprint: `munaqfit-produccion`.
4. Render crea el servicio **`munaqfit-backend`** (Docker, Free, Oregon).
5. Completa las variables marcadas con `sync: false`:

| Variable | Valor |
|:--|:--|
| `SPRING_DATASOURCE_URL` | JDBC URL de la sección anterior |
| `SPRING_DATASOURCE_USERNAME` | usuario de TiDB |
| `SPRING_DATASOURCE_PASSWORD` | contraseña de TiDB |
| `APP_CORS_ALLOWED_ORIGINS` | `https://munaq-fit-rho.vercel.app` |

`render.yaml` ya define el resto:

- `SPRING_PROFILES_ACTIVE=prod`
- `APP_JWT_SECRET` generado automáticamente
- `APP_JWT_EXPIRATION_MS=900000`
- `APP_JWT_REFRESH_EXPIRATION_MS=604800000`
- `APP_CORS_ALLOW_ALL=false`
- `APP_RATE_LIMIT_LOGIN_MAX=10`, `APP_RATE_LIMIT_LOGIN_WINDOW=60`
- `SWAGGER_ENABLED=false`, `SPRING_JPA_SHOW_SQL=false`, `LOG_LEVEL=INFO`
- `IZIPAY_MOCK=true` para la demo sin credenciales sandbox

**Health check:** `/api/health`.

Cuando termine el build, la URL es:

```text
https://munaqfit-backend.onrender.com
```

> Render Free duerme por inactividad. La primera petición puede tardar ~50 s.

---

## 4. Frontend en Vercel

1. En Vercel: **Add New → Project** e importa `YallicoNB/MunaqFit`.
2. **Root Directory:** `frontend`.
3. Framework Preset: **Angular**.
4. Build Command: `npm run build`.
5. Output Directory: `dist/frontend/browser`.
6. Deploy.

El dominio de producción actual es:

```text
https://munaq-fit-rho.vercel.app
```

`frontend/vercel.json` ya incluye el rewrite SPA, por lo que rutas como
`/dashboard` o `/admin/usuarios` funcionan al recargar.

### URL del backend en el frontend

`frontend/src/environments/environment.prod.ts` debe apuntar a Render:

```ts
export const environment = {
  production: true,
  apiUrl: 'https://munaqfit-backend.onrender.com/api',
};
```

Si cambia el dominio de Render, actualiza este archivo, haz commit y push a
`main` para que Vercel redepliegue.

---

## 5. CORS

En Render, `APP_CORS_ALLOWED_ORIGINS` debe coincidir exactamente con el dominio
de Vercel:

```text
https://munaq-fit-rho.vercel.app
```

Si usas un dominio adicional, sepáralos con coma:

```text
https://munaq-fit-rho.vercel.app,https://otro-dominio.vercel.app
```

Después de cambiar una variable de entorno, Render reinicia el servicio.

---

## 6. Verificar el despliegue

| Prueba | URL / acción | Resultado esperado |
|:--|:--|:--|
| Frontend | <https://munaq-fit-rho.vercel.app> | Pantalla de login |
| Backend vivo | <https://munaqfit-backend.onrender.com/api/health> | `{"status":"UP"}` |
| Login | admin `12345678` / `12345678` | Entra al dashboard con rol `ADMIN` |
| Catálogo | Menú **Ventas** | 21 bebidas activas |
| POS | Agregar bebida → Local → Mesa → Cobrar | Subtotal, IGV 18 % y total |
| Pago tarjeta | Método **TARJETA** → Confirmar cobro | Pedido registrado con pago simulado |
| CORS | Login desde el dominio de Vercel | Sin errores en la consola |

Credenciales demo:

- Admin: `12345678` / `12345678`
- Empleado: `11111111` / `12345678`
- Usuario inactivo: `22222222`

---

## 7. Volver a local

```bash
cd backend && mvn spring-boot:run    # http://localhost:8080
cd frontend && npm start             # http://localhost:4200
```

El perfil local usa `application-local.properties` (ignorado por Git) y el
frontend usa `environment.ts` con `http://localhost:8080/api`.

---

## 8. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|:--|:--|:--|
| Render no encuentra el puerto | Falta `PORT` | El Dockerfile ya usa `-Dserver.port=$PORT` |
| Error de CORS en el navegador | Origen distinto al configurado | Ajusta `APP_CORS_ALLOWED_ORIGINS` y reinicia Render |
| La app carga pero no conecta | `environment.prod.ts` con URL vieja | Corrige la URL y redeploya Vercel |
| Vercel da 404 al recargar `/dashboard` | Falta rewrite SPA | Ya está en `frontend/vercel.json` |
| La base no conecta | JDBC URL o TLS mal configurado | Revisa host, puerto `4000` y `useSSL=true&requireSSL=true` |
| Faltan datos en la base | Se esperaba `seed.sql` | Flyway ya carga los datos; no ejecutes `seed.sql` |
| La primera carga tarda ~50 s | Render Free estaba dormido | Es normal; espera o usa un ping a `/api/health` |
