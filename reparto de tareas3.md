# Reparto de Tareas 3 — MunaqFit

> **Tercera etapa del proyecto.** El MVP ya está construido, integrado y auditado
> (ver `reparto de tareas.md` y `reparto de tareas2.md`). Este documento es la
> **guía de trabajo para los 4 desarrolladores** de esta etapa: qué hace cada uno,
> en qué orden, con qué credenciales y bajo qué reglas, para avanzar **sin
> pisarnos ni subir nada a GitHub sin permiso**.
>
> **Objetivo de la etapa:** que la página **realmente funcione** — pagos con
> Izipay, seguridad y contratos de API cerrados, alertas en tiempo real y los
> huecos funcionales cubiertos — y que se vea como **un solo producto
> profesional**.
>
> Todo lo que aparece aquí sale de **código auditado hoy**, no de suposiciones.

---

## 1. Reglas de oro (leen esto primero, todos)

| # | Regla | Por qué |
|:--|:------|:--------|
| 1 | **NO se hace `git push` a GitHub hasta que el dueño del repo lo autorice explícitamente** | Instrucción del equipo. Trabajamos en ramas locales `p1-…`, `p2-…`, `p3-…`, `p4-…` |
| 2 | **`git pull` antes de arrancar el backend** | La base es compartida; si tu código es más viejo que las migraciones aplicadas, Flyway **no deja arrancar** |
| 3 | **Cada quien toca solo sus archivos** (tabla §9) | Si necesitas un archivo de otro, **avísale**, no lo edites |
| 4 | **Secretos nunca en git**: solo variables de entorno o `application-local.properties` (ignorado por git) | Izipay, JWT y la base tienen credenciales |
| 5 | **Las pruebas de bloqueo de login solo sobre la cuenta de sacrificio** (§3) | El bloqueo (3 intentos → 5 min) vive en la **base compartida**: si bloquee la cuenta que usan todos, **helo al equipo entero** |
| 6 | Tarea terminada = **checklist de §11** completo | Compila, probado en local, Swagger, commit en tu rama, aviso en el grupo |

---

## 2. Estado de partida (auditoría de código, 20/10/2026)

**16 requisitos completos, 13 parciales, 3 no implementados.** Este es el mapa
de huecos que repartimos en §5–§8:

| REQ | Estado | Qué falta (real, verificado en código) | Persona |
|:----|:-------|:----------------------------------------|:--------|
| REQ-004 Recuperación de contraseña | Parcial | Backend **sí** tiene `/api/auth/forgot-password` y `/reset-password`; **no existe** ni el enlace en el login ni la pantalla | **P3** |
| REQ-007 Listar empleados | Parcial | `pages/admin/usuarios` lista, pero **faltan** búsqueda por DNI/nombre, filtro ACTIVO/INACTIVO y último inicio de sesión. `/empleados` es un stub vacío (clase sin código) | **P3** |
| REQ-014 Órdenes del día (empleado) | Parcial | Backend devuelve **todas** las ventas (`findAll`), no filtra por empleado ni por día; UI sin filtros | **P2** (backend) + **P3** (UI) |
| REQ-015 Todas las órdenes (admin) | Sin UI | Backend `GET /api/admin/ventas` **existe**; ningún componente lo consume | **P3** |
| REQ-017 Ver receta | Sin UI | `producto.service.recetaDe()` existe, **cero usos** en pantallas | **P3** |
| REQ-018 Info nutricional | Sin UI | Columnas de `V3` existen en BD; **ninguna** vista las muestra | **P3** |
| REQ-019/020 Stock y reabastecimiento | Parcial | Faltan columnas: categoría, unidad, última reposición, caducidad, proveedor, quién reabasteció | **P2** (DTO) + **P3** (tabla) |
| REQ-021/031 Alertas de stock | Parcial | Solo banners al cargar. **Cero** SSE/WebSocket en todo el proyecto | **P1** (SSE) + **P3** (UI) |
| REQ-022 Kardex | Parcial | UI completa, **pero `ReporteKardexController` NO tiene `@PreAuthorize`** → cualquier empleado autenticado podría leer el reporte admin | **P2** (1 línea, URGENTE) |
| REQ-023/024/025 Fidelización | Sin UI | Backend `EmpleadoFidelidadController` **existe**; servicios frontend **existentes**; **cero usos** en pantallas. BD: 3 clientes, **0 visitas** | **P3** |
| REQ-026 Proveedores | Sin UI | CRUD completo en `admin.service.ts`; **nadie lo consume** (la pantalla no existe) | **P3** |
| REQ-029/030/032 Reportes | Parcial | Datos OK, pero **sin gráficos, sin export y sin períodos** día/semana/mes | **P3** + estilo de **P4** |
| Paginación | **No existe** | Ningún endpoint expone `Pageable` | **P2** |
| Rate limiting | **No existe** | Sin Bucket4j ni equivalente | **P2** |
| Refresh token | **No existe** | Solo JWT simple de 24 h | **P2** |
| Izipay | **No existe** | Cero menciones de izipay/culqi; `PagoService` es simulado | **P1** |
| Navegación del admin | Rota | El navbar tiene **2 enlaces idénticos** para admin y empleado: el admin **no puede navegar** a dashboard/inventario/usuarios/reportes/kardex | **P3** aplica / **P4** diseña |
| Diseño consistente | Parcial | **11 CSS aislados** por vista, sidebar y footer existen sin usarse, responsivo incompleto | **P4** |

**Ya cumplido y NO se toca** (no reasignar, solo conservar): arquitectura MVC,
JWT + roles + guards, BCrypt(10) + CORS, Swagger con ~43 operaciones,
`GlobalExceptionHandler` con 17 manejadores (formato de error ratificado),
Flyway V1–V4, POS de ventas con Signals, concurrencia `@Version` → 409, tabla
N-N, Bootstrap 5, servicio de Toast.

**Requisitos fuera de alcance (documentar como no implementados):** REQ-027
(Pedidos a proveedores) y REQ-028 (Control de entregas) — módulos completos de
baja prioridad, **opcionales solo si sobra tiempo**; REQ-009 (cambio de
contraseña auto-gestionado por correo) — requiere SMTP que el proyecto no tiene.

---

## 3. Credenciales compartidas (las mismas para los 4)

| DNI | Contraseña | Rol | Estado | Para qué se usa |
|:----|:-----------|:----|:-------|:----------------|
| `12345678` | `12345678` | ADMIN | ACTIVO | Pruebas de admin (rutas `/admin/*`) |
| `11111111` | `12345678` | EMPLEADO | ACTIVO | Pruebas de empleado (REQ-014, guards, 403) |
| `87654321` | `12345678` | EMPLEADO | ACTIVO | **⚠️ CUENTA DE SACRIFICIO:** única cuenta permitida para probar el **bloqueo por intentos** (REQ-003) y cualquier prueba que pueda fallar 3 veces |
| `22222222` | `12345678` | EMPLEADO | INACTIVO | Verificar que un usuario eliminado **no** puede iniciar sesión (REQ-008) |

- **Regla:** nunca pruebes bloqueos ni contraseñas erróneas sobre `12345678`
  ni `11111111`. Si pruebas el bloqueo sobre `87654321`, **avisa en el grupo**
  (5 min de espera para quien lo necesite usar).
- Los JWT duran 24 h; si un token "deja de funcionar", es que venció: vuelve a
  hacer login.

---

## 4. Base de datos compartida (opción B)

**Un solo clúster TiDB, dos esquemas:**

| Esquema | Quién lo usa | Para qué |
|:--------|:-------------|:---------|
| `munaqfit` | **Solo Render (producción)** | Datos reales del deploy |
| `munaqfit_dev` | **Los 4 devs desde local** | Desarrollo diario, sembrado con dump de producción |

**Credenciales del equipo (usuario restringido, solo ve `munaqfit_dev`):**

```properties
# backend/src/main/resources/application-local.properties  (NUNCA se sube a git)
spring.datasource.url=jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/munaqfit_dev?sslMode=REQUIRED&serverTimezone=America/Lima&allowPublicKeyRetrieval=true
spring.datasource.username=272nBWgPwrYcTkf.dev_munaq
spring.datasource.password=MunaqDev2026
spring.datasource.hikari.maximum-pool-size=4
spring.jpa.show-sql=false
```

**Cómo arrancar el backend en local:**

```powershell
# PowerShell (desde backend/)
$env:SPRING_PROFILES_ACTIVE='local'
mvn spring-boot:run
```

En IntelliJ: *Run → Edit Configurations → Environment variables*:
`SPRING_PROFILES_ACTIVE=local`. Spring carga `application-local.properties`
solo con ese perfil; **sin él**, intenta conectarse a `localhost` y falla.

**Reglas de la base:**

- El usuario `dev_munaq` **solo** tiene permisos sobre `munaqfit_dev` (SELECT,
  INSERT, UPDATE, DELETE y DDL). **No puede tocar `munaqfit`** — por diseño.
- **Siembra:** `munaqfit_dev` se llenó con un dump de producción (20/10/2026).
  Si alguien la "rompe", se vuelve a sembrar con un dump nuevo (**avisar en el
  grupo antes: borra el trabajo no commiteado de todos**).
- **`git pull` antes de arrancar** (regla 2). Si al arrancar ves un error de
  *Flyway validate*, tu código está viejo: haz pull y vuelve a arrancar.
- Nunca te conectes a `munaqfit` (producción) desde local, ni corrijas datos
  de producción a mano.

---

## 5. Convenciones globales (acuerdos del equipo)

| Tema | Acuerdo |
|:-----|:--------|
| **Errores de API** | `{timestamp, status, message}` — ya lo hace `GlobalExceptionHandler`. **No se cambia**, solo se ratifica |
| **Paginación** | Request `?pagina=0&tamano=20` (por defecto `tamano=20`, máximo `100`) → respuesta `{contenido, pagina, tamano, totalElementos, totalPaginas}`. La implementa **P2**; las UIs la consumen |
| **Nombres de componentes Angular** | Sin sufijo `.component.ts` (como `login.ts`, `ventas.ts`). `kardex.component.*` es la única excepción y se renombra (tarea 3.11) |
| **Migraciones Flyway** | Bloques reservados: **P1 = V10–V19**, **P2 = V20–V29**, **P3 = V30–V39**, **P4 = V40–V49**. No tocar V1–V4 (ya aplicadas). Cada quien escribe **solo** en su bloque |
| **Prettier** | `printWidth: 100` — crear `.prettierrc` el día 1 (tarea 3.12) |
| **Secretos** | Solo env vars o `application-local.properties`. `.env`, `*.env` y `application-local.properties` están en `.gitignore` |
| **Swagger** | Todo endpoint nuevo lleva `@Operation` y queda visible en `/v3/api-docs` |
| **Ramas** | `p1-izipay-token`, `p2-kardex-preauthorize`, `p3-fidelidad-ui`, `p4-tokens-diseno`, etc. **Push solo cuando se autorice** |
| **Idioma** | UI, mensajes y respuestas en español |

---

## 6. Orden de prioridad de la etapa

| Prioridad | Qué | Quién | Notas |
|:---------:|:----|:------|:------|
| **1** | **Izipay (sandbox)** | P1 | Empezar el **día 1**: las credenciales son externas y tardan en llegar. Cuenta como "integración con API pública" del rúbrica |
| **2** | **Seguridad + contratos** | P2 | Empieza por el fix de 1 línea del kardex; luego rate limiting, sanitización, refresh token, paginación, Swagger |
| **3** | **SSE de alertas de stock** | P1 | Barato y hace que "tiempo real" sea innegable en la sustentación |
| **4** | **Huecos funcionales** | P3 | Sin dependencias de nadie: REQ-004, 015, 017, 018, 023–026, 029–032, navbar |
| **—** | **Diseño (11 pantallas)** | P4 (Yazid) | En paralelo desde el día 1: primero tokens y navbar, luego rediseño pantalla por pantalla |
| opcional | REQ-027 / REQ-028 | — | Solo si sobra tiempo; si no, se documentan como no implementados |

---

## 7. Persona 1 — Integraciones (Izipay + tiempo real)

**Objetivo:** cobrar de verdad con Izipay y que las alertas de stock lleguen
en tiempo real.

| # | Tarea | Archivos | Detalle |
|:--|:------|:---------|:--------|
| 1.1 | **Pedir credenciales sandbox de Izipay al dueño** | — | **Día 1.** Fase 1 = sandbox (URLs y llaves de prueba); Fase 2 = credenciales reales (solo cambian env vars, el código no) |
| 1.2 | Token de pago server-side | `controller/IzipayController.java`, `service/IzipayService.java` (nuevos) | `POST /api/get-izipay-token`: llamada desde el backend con `merchantCode`, `amount`, `currency=PEN` → devuelve `tokenGUID`. Credenciales en env vars (`IZIPAY_*`), nunca en el código |
| 1.3 | SDK en el frontend | `pages/ventas/ventas.html`, `service/izipay.service.ts` (nuevo) | `LoadForm(token, keyRSA)` en el modal de pago; `callbackResponse` con `Code:"00"` = pago exitoso |
| 1.4 | Registrar el pago real | `service/PagoService.java` | Éxito → venta **PAGADA** en BD (monto exacto digital, sin vuelto); rechazo/cancelación → mensaje claro, la venta queda pendiente |
| 1.5 | Verificación en vivo | — | Pago con tarjeta de prueba sandbox y captura para la sustentación |
| 1.6 | SSE backend | `controller/AlertaSseController.java`, `service/AlertaSseService.java` (nuevos) | `GET /api/empleado/alertas/stock` con `SseEmitter`. Evento cuando el stock baja a ≤ crítico (dispara en venta y en reabastecimiento). Ping keep-alive cada ~25 s (Render corta conexiones quietas) |
| 1.7 | SSE frontend | `service/alerta-sse.service.ts` (nuevo), enganche en `app.ts` | Suscripción global con `EventSource`; al recibir evento → `ToastService.exito()/warning()` (el servicio de Toast **ya existe**). *Nota:* `EventSource` no manda cabeceras `Authorization` → enviar token por `?token=` y validarlo en backend |
| 1.8 | Alerta de urgencia | — | Mientras P3 construye la UI de alertas (REQ-021), el toast SSE ya cumple la demo de "tiempo real" |

**Bloqueo:** 1.1 (credenciales externas) — **se pide el día 1, no se espera para
empezar 1.2/1.6** (todo el código se escribe contra sandbox; solo faltan las env vars).

---

## 8. Persona 2 — Backend: contratos y seguridad

**Objetivo:** que la API esté blindada y con contratos claros que el frontend
pueda consumir sin sorpresas.

| # | Tarea | Archivos | Detalle |
|:--|:------|:---------|:--------|
| **2.1** | **URGENTE: proteger el Kardex** | `controller/Admin/ReporteKardexController.java` | Agregar `@PreAuthorize("hasRole('ADMIN')")` a la clase (hoy solo tiene `@CrossOrigin`). **1 línea, es lo primero de la etapa.** Verificar después que ningún otro controller admin falte |
| 2.2 | Rate limiting en login | `pom.xml`, `config/RateLimitConfig.java` (nuevo) | Bucket4j (o filtro propio): p.ej. 10 req/min por IP a `/api/auth/login` → **429** con el formato de error estándar |
| 2.3 | Sanitización de entradas | `service/` o `config/` | `trim` + eliminación de etiquetas HTML/JS en campos de texto (usuarios, productos, motivos) — cierra XSS almacenado |
| 2.4 | Refresh token | `AuthController`, `JwtTokenProvider` | Access token corto (15 min) + refresh (7 días) en `POST /api/auth/refresh`; logout limpia ambos. Refrescar en el frontend sin perder la sesión |
| 2.5 | **Paginación** | controllers de listados | Contrato de §5 en: `GET /api/admin/usuarios`, `/api/admin/inventario`, `/api/admin/reportes/kardex`, `/api/admin/ventas`. **Avisar a P3** cuando cada uno esté listo (él consume el contrato) |
| 2.6 | REQ-014: filtrar órdenes | `EmpleadoOrdenController`/`Service` | Historial del empleado = solo ventas **del JWT** y **de hoy**; para admin, filtros de fecha/empleado/estado en `GET /api/admin/ventas` |
| 2.7 | REQ-019/020: DTO de inventario | `dto/`, `AdminInventarioController` | Exponer categoría, unidad de medida, última reposición, caducidad y proveedor. Si falta columna en BD → migración en **su bloque V20–V29** |
| 2.8 | Swagger al día | todos los anteriores | Cada endpoint nuevo documentado; verificar que `/v3/api-docs` sigue respondiendo |
| 2.9 | Ratificar, no romper | `exception/GlobalExceptionHandler.java` | El manejo de errores ya cumple el contrato `{timestamp,status,message}` — no lo reescribas |

---

## 9. Persona 3 — Frontend: huecos funcionales

**Objetivo:** que ninguna pantalla quede a medias. Todas sus tareas **no
dependen de nadie** — puede arrancar hoy.

| # | Tarea | Archivos | Detalle |
|:--|:------|:---------|:--------|
| **3.1** | **Navbar por rol** | `layouts/navbar/navbar.ts/.html` | Hoy: 2 enlaces idénticos → **el admin no puede navegar**. Admin ve: Dashboard, Inventario, Usuarios, Reportes, Kardex, Ventas, Órdenes; empleado ve: Ventas, Menú, Órdenes, Cuenta. **Solo agrega la lógica y los enlaces** — el rediseño visual es de Yazid (4.2): avísale cuando esté listo para que aplique el estilo |
| 3.2 | REQ-004 recuperar contraseña | `pages/auth/login/*`, `service/auth.service.ts` | El backend **ya tiene** `/forgot-password` y `/reset-password`: agregar enlace "¿Olvidaste tu contraseña?" y la pantalla de nueva contraseña |
| 3.3 | REQ-015 órdenes admin | `pages/admin/ordenes/` (nuevo) o en reportes | Consumir `GET /api/admin/ventas` (ya está en `admin.service.ts:77`), con filtros fecha/empleado/estado y botón de cambio de estado |
| 3.4 | REQ-017 receta | `pages/menu/menu.*` | Modal o ficha técnica con `recetaDe()` (servicio ya existe, **cero usos**) |
| 3.5 | REQ-018 info nutricional | `pages/menu/menu.*`, `models/producto.ts` | Mostrar calorías/proteínas/etc. (columnas `V3` ya en BD). Sin datos → *"Información nutricional no disponible"* |
| 3.6 | REQ-023/024/025 fidelización | `pages/ventas/ventas.*`, `pages/admin/clientes/` | Registro de cliente frecuente desde el POS, suma de visitas al cobrar y premio a la décima visita (backend + `venta.service.ts:61-67` ya listos, **0 usos**) |
| 3.7 | REQ-026 proveedores | `pages/admin/proveedores/` (nuevo), `app.routes.ts` | CRUD completo con `admin.service.ts:59-72` (ya existe) + ruta con `adminGuard` + enlace en navbar |
| 3.8 | REQ-007 empleados | `pages/admin/usuarios/*`, `pages/empleados/` | Búsqueda por DNI/nombre, filtro ACTIVO/INACTIVO, columna de último inicio de sesión; **eliminar el stub `/empleados`** (duplica usuarios) |
| 3.9 | REQ-029/030/032 reportes | `pages/admin/reportes/*`, `package.json` | Períodos día/semana/mes, gráficos con Chart.js y export. **El estilo lo define Yazid (4.7)** — primero datos, después look |
| 3.10 | Paginación en UI | listas de usuarios/inventario/kardex | Consumir el contrato `{contenido,…}` de P2 (2.5) cuando él avise que está listo |
| 3.11 | Renombrar kardex | `pages/admin/kardex/kardex.component.* → kardex.*` | Convención de nombres (§5) |
| 3.12 | Crear `.prettierrc` | raíz del repo | `{ "printWidth": 100 }` — **día 1**, avisar a los demás |
| 3.13 | Estados consistentes | sus vistas | Cargando / error / vacío en todas las vistas que toque, con las clases que defina Yazid (4.5) |

---

## 10. Persona 4 — Yazid · Diseño

> **Objetivo: que las 11 pantallas se vean como un solo producto profesional.**
>
> Las 11 pantallas: **login, home (landing), cuenta, ventas (POS), menú,
> órdenes, dashboard, inventario, usuarios, reportes y kardex.** *(La pantalla
> stub `/empleados` se elimina — tarea 3.8 — y no cuenta.)*

| # | Tarea | Archivos | Detalle |
|:--|:------|:---------|:--------|
| **4.1** | **Tokens de diseño** (día 1) | `frontend/src/styles.css` | Consolidar la identidad en variables: paleta `--munaq-*`, tipografía, espaciado, radios, sombras, y clases base (botones, tablas, cards, formularios, badges). **Entregable que usan todos** — comparte el resumen con P3 desde el día 1 |
| **4.2** | **Navbar / sidebar / footer** (día 1–2) | `layouts/**` (CSS) | Rediseño de la navegación **por rol** (mockups listos). El sidebar y el footer **ya existen sin usarse**: decide integrarlos o eliminarlos. **P3 primero pone los enlaces (3.1), tú aplicas el estilo después** — coordinarse |
| 4.3 | Unificar las 11 pantallas | `pages/**/*.css` (los 11 aislados) | Migrar cada CSS de vista a los tokens/Bootstrap de a una. Orden sugerido: **login → ventas → dashboard → reportes** (las más vistas en la demo) |
| 4.4 | Responsivo (RNF-013) | todos los CSS | Los 4 tamaños: **1920×1080, 1366×768, 768×1024 y 375×667** |
| 4.5 | Estados como parte del look | `pages/**/*.css` | Clases para vacío / cargando / error que P3 consume (tarea 3.13) — un solo estilo de "sin datos" en toda la app |
| 4.6 | Accesibilidad visual | todo | Contraste AA, foco visible, objetivos táctiles ≥40 px. P3 agrega `aria-` al maquetar; tú verificas |
| 4.7 | Look de dashboard y reportes | `pages/dashboard`, `pages/admin/reportes` (CSS) | Tarjetas KPI, gráficos y tablas con estilo profesional — P3 los implementa con tus especificaciones |
| 4.8 | Verificación final | — | Recorrer las 11 pantallas (rol admin y rol empleado) en los 4 tamaños: **deben verse como un solo producto** |

**Regla de coordinación:** tú **no** editas `.ts` ni `.html` de las páginas
(salvo ajuste visual mínimo acordado); P3 **no** edita `.css`. Así no hay
conflictos.

---

## 11. Propiedad de archivos

| Persona | Backend | Frontend | Migraciones |
|:--------|:--------|:---------|:------------|
| **P1** | `IzipayController/Service`, `AlertaSseController/Service` | `pages/ventas/*` (modal de pago), `service/izipay.service.ts`, `service/alerta-sse.service.ts` | **V10–V19** |
| **P2** | `controller/` (excepto los de P1), `service/`, `dto/`, `config/`, `pom.xml`, `GlobalExceptionHandler` (solo ratificar) | — | **V20–V29** |
| **P3** | — | `pages/**` (HTML/TS), `service/*.ts` (frontend), `app.routes.ts`, `layouts/navbar/navbar.ts/.html`, `.prettierrc` | **V30–V39** |
| **P4 (Yazid)** | — | **todos los `.css`**, `styles.css`, `layouts/**` (estilo), `shared/` visual | **V40–V49** |

- **Zonas de coordinación:** `navbar` (P3 enlaces → P4 estilo), `dashboard` y
  `reportes` (P3 lógica → P4 look), contrato de paginación (P2 define → P3 consume).
- **`application.properties`** es compartido: cambios mínimos y aviso en el grupo.
- **Base de datos compartida:** todos apuntan a `munaqfit_dev` con el perfil
  `local` (§4). Nadie se conecta a `munaqfit` (producción).

---

## 12. Dependencias y primeras acciones (día 1)

| Persona | Primera acción hoy |
|:--------|:-------------------|
| **P1** | Pedir credenciales sandbox de Izipay al dueño (1.1) → mientras llegan, escribir `IzipayController` y el SSE (1.2, 1.6) |
| **P2** | **Fix del `@PreAuthorize` del kardex (2.1)** → luego rate limiting y sanitización (2.2, 2.3) |
| **P3** | `.prettierrc` (3.12) → navbar por rol (3.1) → REQ-004 y REQ-015 (3.2, 3.3) |
| **P4 (Yazid)** | Tokens de diseño (4.1) → mockup de navbar/sidebar por rol (4.2) |

| Depende | De quién | De qué |
|:--------|:---------|:-------|
| Refresh token en UI (P3) | P2 (2.4) | Flujo nuevo de auth |
| UI paginada (P3 3.10) | P2 (2.5) | Contrato `{contenido,…}` |
| Columnas de inventario (P3) | P2 (2.7) | DTO ampliado |
| Estilo de navbar (P4 4.2) | P3 (3.1) | Enlaces por rol primero |
| Gráficos de reportes (P3 3.9) | P4 (4.7) | Definición visual |
| Pago con Izipay (P1 1.1) | **Dueño** | Credenciales sandbox (día 1) |

Nada más bloquea: **todos pueden trabajar hoy**.

---

## 13. Checklist de "tarea terminada"

Antes de dar una tarea por hecha, verifica **todo**:

- [ ] Compila: `mvn clean compile` (backend) / `ng build` (frontend) sin errores
- [ ] Probado en local contra **`munaqfit_dev`** con las credenciales de §3
- [ ] No rompió endpoints existentes (prueba rápida en `/swagger-ui/index.html`)
- [ ] Si hubo endpoint nuevo: documentado en Swagger
- [ ] Si hubo migración: escrita **solo en tu bloque** V10–V49 y no tocaste V1–V4
- [ ] Ningún secreto en el código (solo env vars / `application-local.properties`)
- [ ] Commit en **tu rama** (`pX-…`), sin archivos de otros
- [ ] Aviso en el grupo: qué cambiaste y cómo probarlo
- [ ] **Ningún push a GitHub sin autorización**

---

## 14. Resumen por persona

| Persona | Rol | Tareas | Prioridad clave | Dificultad |
|:--------|:----|:-------|:----------------|:-----------|
| **P1** | Integraciones (Izipay + tiempo real) | 1.1–1.8 | Izipay sandbox **día 1** | **ALTA** |
| **P2** | Backend: contratos y seguridad | 2.1–2.9 | Fix kardex **ya**, luego rate limit/refresh/paginación | **ALTA** |
| **P3** | Frontend: huecos funcionales | 3.1–3.13 | Navbar + REQs sin dependencia | **MEDIA** |
| **P4 (Yazid)** | Diseño (11 pantallas) | 4.1–4.8 | Tokens y navbar **día 1** | **MEDIA** |

> **Fuera de alcance documentado:** REQ-027 y REQ-028 (opcionales, solo si
> sobra tiempo) y REQ-009 (requiere SMTP). Todo lo demás de la especificación
> está repartido arriba.
