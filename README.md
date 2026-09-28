# MunaqFit Manager

Sistema de gestión para **Munaq Fit**, tienda de bebidas saludables (Ubicación: Av. Independencia 198, Ate).

## Estructura del proyecto

| Carpeta | Descripción |
|:---|:---|
| `backend/` | API REST en Spring Boot (Java 17, Maven, JPA, MySQL, Spring Security + JWT) |
| `frontend/` | Aplicación Angular 20 (standalone components, `ng serve` en el puerto 4200) |
| `database/` | Scripts SQL: `schema.sql` (estructura) y `seed.sql` (datos de prueba) |
| `reparto de tareas.md` | Reparto de tareas por developers / plan de sprints |
| `requerimentos.md` | Documento de requerimientos del sistema |

## Requisitos previos

- **JDK 17** (o superior; el proyecto está targeteado a Java 17)
- **Maven 3.6+**
- **MySQL 8.x** corriendo localmente en el puerto `3306`

## Puesta en marcha

### 1. Base de datos

`schema.sql` es la **única fuente de verdad** de la base: la borra y la
reconstruye desde cero. Ejecutar en orden:

```sql
source database/schema.sql;
source database/seed.sql;
```

O desde la línea de comandos (ajusta usuario/contraseña):

```sh
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

> ⚠️ `schema.sql` empieza con un `DROP DATABASE`. No lo ejecutes contra una
> base con datos reales sin respaldarla.

> **Nota de credenciales:** el `application.properties` está configurado con usuario `root` y contraseña `root123`. Si tu contraseña es distinta, cámbiala en `backend/src/main/resources/application.properties`.

> **Nota:** `spring.jpa.hibernate.ddl-auto=none`. Hibernate nunca modifica el
> esquema. **Hoy** todo cambio de base de datos se hace en `schema.sql` y hay
> que volver a ejecutarlo.
>
> ⚠️ **En transición a Flyway.** La etapa 2 del proyecto (tareas 1.1 y 1.2 de
> `reparto de tareas2.md`) mueve el esquema a Flyway, en
> `backend/src/main/resources/db/migration/`, y sube `ddl-auto` a `validate`.
> A partir de ese momento:
>
> - Los cambios de esquema se hacen en un archivo nuevo `V<n>__nombre.sql`.
> - **Ya no se ejecuta `schema.sql` a mano**, porque su `DROP DATABASE` borraría
>   el historial de migraciones. Se conserva como script de recreación desde cero.
> - Si al arrancar aparece `Found non-empty schema(s) "munaqfit"`, Flyway está
>   pidiendo el `V1__baseline.sql` antes de poder seguir.
>
> Hasta que ese trabajo se integre, el flujo sigue siendo el del párrafo anterior.

### 2. Backend

```sh
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### 3. Usuarios de prueba

Los cuatro usuarios usan la contraseña `12345678`:

| DNI | Contraseña | Nombre | Rol | Estado |
|:---|:---|:---|:---|:---|
| `12345678` | `12345678` | Noe Munaq | ADMIN | ACTIVO |
| `87654321` | `12345678` | Noe Developer | EMPLEADO | ACTIVO |
| `11111111` | `12345678` | Maria Lopez | EMPLEADO | ACTIVO |
| `22222222` | `12345678` | Carlos Ruiz | EMPLEADO | **INACTIVO** |

> Para probar el flujo de venta (`/ventas`, `/ordenes`, `/menu`) hay que entrar con
> un **EMPLEADO**. Con el admin esas rutas se ven igual, pero el flujo de empleado es
> el que se está construyendo.
>
> `22222222` sirve para probar el bloqueo de cuenta (REQ-003): nace `INACTIVO`, así
> que el login se rechaza aunque la contraseña sea correcta, y no cuenta como
> intento fallido. Con 3 intentos fallidos, cualquier cuenta activa queda
> bloqueada 5 minutos.

### 4. Frontend

```sh
cd frontend
npm install
npm start
```

La app queda disponible en `http://localhost:4200`. El CORS del backend ya está
configurado para permitir ese origen.

## Endpoint de autenticación

`POST /api/auth/login`

```json
{
  "dni": "12345678",
  "password": "12345678"
}
```

Responde con un token JWT (`Bearer`) que debe enviarse en el header `Authorization` para acceder al resto de la API.

Después de 3 intentos fallidos la cuenta queda **bloqueada 5 minutos** (REQ-003). Un login
exitoso reinicia el contador y limpia el bloqueo.

## Recuperación de contraseña

`POST /api/auth/forgot-password`

```json
{ "email": "noe.munaq@munaqfit.com" }
```

Devuelve `200` **siempre**, exista o no el correo, con el mismo mensaje. Esto es
intencional: si la respuesta fuera distinta, alguien podría enumerar qué correos están
registrados en el sistema.

```json
{
  "message": "Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña",
  "token": "999f1d0a-7f3a-462a-92e1-b2812c7f462b"
}
```

> En un entorno real el token viajaría por correo y **no** se devolvería en la respuesta.
> Aquí se devuelve porque el proyecto no incluye envío de correo; el token queda
> también registrado en el log del backend.

`POST /api/auth/reset-password`

```json
{ "token": "999f1d0a-...", "newPassword": "nuevaClave123" }
```

| Situación | Respuesta |
|:---|:---|
| Token válido y contraseña de 8+ caracteres | `200` → la contraseña **se cambia** |
| Token inexistente, vacío, vencido o ya usado | `400` |
| Contraseña de menos de 8 caracteres | `400` |

El token es de **un solo uso** (se descarta al cambiar la contraseña) y expira a los
**60 minutos**. Restablecer la contraseña también libera el bloqueo por intentos fallidos.

## Comprobar el token (para el frontend)

`GET /api/auth/validar-token` — solo verifica el token, no devuelve datos de negocio.

| Situación | Respuesta |
|:---|:---|
| Token válido | `200` → `{"valido": true, "dni": "12345678", "rol": "ADMIN"}` |
| Token inválido o expirado | `401` → `{"valido": false, "mensaje": "..."}` |
| Sin token | `401` → `{"valido": false, "mensaje": "..."}` |

```sh
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/auth/validar-token
```

Útil para que el frontend verifique la sesión al cargar la app (por ejemplo en un `interceptor` o un `guard`).

## API de productos

Todas requieren token JWT válido en el header `Authorization: Bearer <TOKEN>`.

| Método | Ruta | Descripción |
|:---|:---|:---|
| `GET` | `/api/productos` | Lista todos los productos |
| `GET` | `/api/productos/{id}` | Obtiene un producto por su id |
| `GET` | `/api/productos/categoria/{categoriaId}` | Lista productos de una categoría |
| `GET` | `/api/productos/buscar?nombre=texto` | Busca productos por nombre (parcial) |

## Resto de endpoints

| Módulo | Prefijo | Developer |
|:---|:---|:---|
| Panel de administración (dashboard, inventario, proveedores, usuarios, ventas, reportes) | `/api/admin/**` | Dev 2 |
| Módulo de empleado (ventas, órdenes, menú, fidelidad) | `/api/empleado/**` | Dev 3 |

Los endpoints `/api/admin/**` requieren rol `ADMIN`.

### Flujo de venta y pago (módulo de empleado)

Registrar un pedido **descuenta el stock de los insumos** según la receta de cada
bebida y deja el movimiento registrado en el kardex (`movimiento_inventario`):

| Método | Ruta | Descripción |
|:---|:---|:---|
| `POST` | `/api/empleado/ventas/registrar` | Crea la venta: calcula IGV, guarda detalles, descuenta stock |
| `POST` | `/api/empleado/ventas/pago` | Registra el pago, calcula el vuelto y pasa la venta a `PAGADO` |
| `POST` | `/api/empleado/ventas/vuelto` | Solo calcula el vuelto, sin registrar nada |

```jsonc
// POST /api/empleado/ventas/registrar
{
  "venta": { "mesa": 5, "tipoVenta": "LOCAL", "notas": "Para llevar" },
  "detalles": [{ "bebida": { "id": 1 }, "cantidad": 2 }]
}
```

El empleado que registra la venta se toma del token JWT, no del cuerpo del
request. Si un insumo no tiene stock suficiente la venta se rechaza y no se
guarda nada.

```jsonc
// POST /api/empleado/ventas/pago
{
  "ventaId": 1,
  "montoPagado": 50,
  "tipoPago": "YAPE",          // EFECTIVO | YAPE | PLIN | TRANSFERENCIA | QR
  "numeroOperacion": "998877"
}
```

Responde con el vuelto y los estados resultantes:

```json
{
  "pagoId": 1, "numeroPedido": "PED-000001",
  "montoTotal": 37.76, "montoPagado": 50, "vuelto": 12.24,
  "tipoPago": "YAPE", "estadoPago": "COMPLETADO", "estadoVenta": "PAGADO"
}
```

No se puede pagar dos veces la misma venta ni pagar con un monto menor al total.

### Inventario, usuarios y proveedores (admin)

| Método | Ruta | Descripción |
|:---|:---|:---|
| `GET` | `/api/admin/inventario` | Lista insumos con su stock |
| `GET` | `/api/admin/inventario/critico` | Insumos en stock crítico |
| `GET` | `/api/admin/inventario/bajo` | Insumos por debajo del mínimo |
| `POST` | `/api/admin/inventario/reabastecer` | Agrega stock y registra el ingreso en el kardex |
| `GET` | `/api/admin/usuarios` | Lista empleados |
| `POST` | `/api/admin/usuarios` | Crea un empleado (siempre con rol `EMPLEADO`) |
| `DELETE` | `/api/admin/usuarios/{id}` | Inactiva al empleado (soft delete) |
| `GET` | `/api/admin/proveedores` | Lista proveedores |
| `POST` | `/api/admin/proveedores` | Crea un proveedor |
| `PUT` | `/api/admin/proveedores/{id}` | Actualiza un proveedor |
| `DELETE` | `/api/admin/proveedores/{id}` | Elimina un proveedor, o lo da de baja si tiene productos asociados |

## Frontend: estructura y servicios

Rutas registradas en `app.routes.ts`:

| Ruta | Componente | Acceso |
|:---|:---|:---|
| `/` | Login | solo invitados |
| `/home` | Home | autenticado |
| `/cuenta` | Cuenta (datos de la sesión) | autenticado |
| `/ventas` | ⚠️ sin desarrollar (ver nota) | autenticado |
| `/empleados` | ⚠️ sin desarrollar (ver nota) | autenticado |
| `/menu` | Menu | `ADMIN` o `EMPLEADO` |
| `/ordenes` | Ordenes | `ADMIN` o `EMPLEADO` |
| `/dashboard` | Dashboard | solo `ADMIN` |
| `/admin/inventario` | Inventario | solo `ADMIN` |
| `/admin/usuarios` | Usuarios | solo `ADMIN` |
| `/admin/reportes` | Reportes | solo `ADMIN` |

Piezas clave para el desarrollo del frontend:

| Archivo | Para qué sirve |
|:---|:---|
| `interceptors/auth.interceptor.ts` | Agrega `Authorization: Bearer <token>` a toda llamada a la API y, si el backend responde `401`, cierra la sesión y vuelve al login. Ya está registrado en `app.config.ts`. |
| `guard/role.guard.ts` | `adminGuard` y `empleadoGuard` para proteger las rutas por rol. |
| `service/auth/auth.service.ts` | `validarToken()` contra `GET /api/auth/validar-token` y `comprobarSesion()` para validar al arrancar. |
| `service/producto.service.ts` | Productos e insumos de una receta. |
| `service/venta.service.ts` | Ventas, pagos, órdenes, menú y fidelidad. |
| `service/admin.service.ts` | Inventario, usuarios, proveedores y ventas (admin). |
| `service/reporte.service.ts` | Métricas del dashboard y reportes con filtro de fechas. |
| `core/api.ts` | URL base del backend, para no repetirla en cada servicio. |
| `layouts/sidebar/` | Menú lateral reutilizable, con enlaces según el rol. |
| `service/auth/token.ts` | Guarda la sesión en `localStorage`. Además del token, el nombre y el rol, persiste el **DNI y el correo**, que son los que consume la página `Mi cuenta`. |

> ⚠️ **Nota sobre `/ventas` y `/empleados`.** Ambas páginas se generaron con `ng generate`
> y nunca se desarrollaron: su HTML es literalmente `<p>ventas works!</p>` y tienen el CSS
> en 0 bytes. No aparecen en el sidebar (su función la cubren `/ordenes` y
> `/admin/usuarios`), pero **el navbar de `/home` sí las enlaza cuando el rol es `ADMIN`**.

> ℹ️ **Sobre `/register`.** Se eliminó del proyecto. `requerimentos.md` no pide registro
> público — el alta de personal se hace con `POST /api/admin/usuarios` (REQ-006). El
> backend nunca tuvo `POST /api/auth/register`, solo un `permitAll` que lo anunciaba.

## Modelo de datos

14 tablas en MySQL 8. `schema.sql` las crea con 33 restricciones `CHECK`,
10 `UNIQUE`, 13 claves foráneas y 0 columnas `TIMESTAMP`.

### Decisiones de normalización

| Decisión | Por qué |
|:---|:---|
| `categoria` (insumos) y `categoria_bebida` (bebidas) son tablas separadas | Antes `bebida.categoria` era texto suelto: tenía 8 valores (Detox, Energizante…) que **no coincidían con ninguno** de los 6 de `categoria`, y nada impedía escribir un nombre nuevo con error. Ahora la bebida tiene una FK real. |
| `receta` ya **no** tiene columna `unidad` | La cantidad va siempre en la unidad canónica del producto (`producto.unidad_medida`). Duplicar la unidad obligaba a convertir G↔KG y ML↔L en cada venta, y una receta podía contradecir a su insumo. Se borraron ~115 líneas de código de conversión. |
| `pago` ya **no** tiene `monto_total` | Se derivaba de `venta.total`: una dependencia transitiva y una violación de 3FN. Se lee de `venta` donde el dato vive. |
| `producto` ya **no** tiene `precio_venta` | El insumo no se vende solo, se vende dentro de una bebida, y ese precio está en `bebida.precio`. Era una columna que nadie llenaba. |
| La tasa de IGV vive en la tabla `parametro` | Estaba fija en `VentaService` como constante. Ahora se cambia con un `UPDATE` y sin recompilar. |

### Instantáneas y auditing

- `venta.igv_tasa` guarda la tasa de IGV que se aplicó a esa venta. Si mañana la
  tasa sube, los reportes de hoy siguen dando el resultado correcto.
- `detalle_venta.precio_unitario` y `subtotal` son el precio del momento de la
  venta: cambiar el precio de una bebida no altera las ventas ya emitidas.
- `producto.stock_actual` es un caché. La fuente de verdad es
  `movimiento_inventario` (kardex) y se actualiza en la **misma transacción**.
- `movimiento_inventario.tipo_referencia` acompaña a `referencia_id`, que al ser
  polimórfico no puede tener FK. Hoy solo se emite `VENTA`; los demás valores
  quedan listos para el REQ-027 (pedidos a proveedor), que aún no existe.

### Reglas que ahora aplica la propia base

MySQL 8.0.16+ soporta `CHECK`, así que lo que se puede expresar sobre una tabla
se hace en el esquema y no solo en Java. Ejemplos, todos verificados con
intentos de escritura que la base rechaza:

- `stock_actual >= 0`, y nunca `stock_critico > stock_minimo`
- `receta.cantidad > 0`
- `receta` no puede repetir el mismo insumo dentro de una bebida (`UNIQUE`) —
  antes eso habría descontado el stock dos veces
- `subtotal = cantidad * precio_unitario` y `total = subtotal + igv` y
  `igv = ROUND(subtotal * igv_tasa, 2)`
- `pago.monto_pagado > 0` y `pago.monto_cambio >= 0`
- una venta no puede tener dos pagos (`UNIQUE (venta_id)`)
- una venta no puede contar dos veces para fidelidad (`UNIQUE (venta_id)`)
- `cliente_fidelidad.dni` es `UNIQUE`: sin él, el mismo cliente podía
  registrarse tantas veces como el empleado quisiera y acumular visitas
  infinitas

### Lo que la base NO puede validar

- `pago.monto_cambio = pago.monto_pagado - venta.total`: MySQL no admite
  subconsultas dentro de `CHECK`. Lo valida `PagoService`.
- Que el vuelto solo exista en efectivo: también es regla de negocio, no de tabla.
- `venta.mesa` no tiene `CHECK` porque la app todavía no la envía.

## Flujo de trabajo en Git

La rama por defecto es `main` y **no recibe commits directos**. Cada tarea de
`reparto de tareas2.md` va en su propia rama y entra por Pull Request.

| Rama | Developer | Tarea |
|:---|:---|:---|
| `feat/flyway-baseline` | Dev 1 | 1.1 – 1.2 · migraciones a Flyway |
| `feat/nn-proveedor` | Dev 1 | 1.3 – 1.5 · relación muchos-a-muchos |
| `feat/pos-ventas` | Dev 1 | 1.6 – 1.9 · pantalla de ventas |
| `fix/concurrencia-pedido` | Dev 1 | 1.10 – 1.11 · `@Version` y `numero_pedido` |
| `feat/kardex-backend` | Dev 2 | 2.1 – 2.5 · reporte y export CSV |
| `feat/kardex-pantalla` | Dev 2 | 2.6 – 2.7 · vista `/admin/kardex` |
| `feat/bootstrap-responsive` | Dev 3 | 3.1 – 3.3 · Bootstrap 5 y responsive |
| `feat/shared-components` | Dev 3 | 3.4 – 3.6 · `shared/` y toast |
| `feat/productos-crud` | Dev 4 | 4.1 – 4.3 · DTOs, CRUD de productos y `PUT` de usuarios |
| `feat/accesibilidad-formularios` | Dev 4 | 4.4 – 4.8 · accesibilidad y reactive forms |

Reglas:

1. `main` no se actualiza salvo por un PR.
2. **Una rama por tarea**, no una por persona: así el PR se reviewa corto.
3. Antes de abrir el PR, `mvn clean compile` (en `backend/`) y `ng build` (en
   `frontend/`) tienen que salir limpios.
4. El PR describe qué requisito cierra (`REQ-xxx`) y qué archivos toca.
5. **No se edita un archivo que la tabla de `reparto de tareas2.md` le asigna a
   otro developer.** Si lo necesitas, avisa en el grupo en vez de cambiarlo.
6. El PR lo aprueba **otro** developer, nunca el autor.
7. Las ramas de la primera etapa ya están dentro de `main` y se pueden borrar en
   GitHub. La excepción es `completa-huecos-backend-y-frontend`, que **no** está
   mergeada: no la abras ni la borres sin revisar, porque pisa el estado actual
   del proyecto.
