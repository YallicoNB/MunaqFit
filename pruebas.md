# MunaqFit — Guía de pruebas

Todo lo que puedes hacer en el proyecto: credenciales, pasos, pruebas de la API y consultas SQL para ver la base de datos.

---

## 1. Levantar el proyecto

**Requisitos:** MySQL 8, Java 17+, Maven, Node 18+.

### Paso 1 — Base de datos

Crea la base (una sola vez):

```sql
CREATE DATABASE munaqfit;
```

- Usuario: `root`
- Contraseña: `root123`
- Puerto: `3306`

### Paso 2 — Backend (terminal 1)

```bash
cd backend
mvn spring-boot:run
```

Espera el mensaje `Started BackendApplication`. Queda en `http://localhost:8080`.

> **Flyway** crea solo todas las tablas al arrancar. No necesitas crear nada a mano.

### Paso 3 — Cargar los datos de prueba (una sola vez)

Se hace **después** de que el backend arrancó (para que las tablas ya existan):

```bash
mysql -u root -proot123 munaqfit < database/seed.sql
```

### Paso 4 — Frontend (terminal 2)

```bash
cd frontend
npm install      # solo la primera vez
npm start
```

Abre **http://localhost:4200**

---

## 2. Credenciales

La contraseña de **todos** es `12345678`.

| Rol | DNI | Nombre | Contraseña | Estado |
|:--|:--|:--|:--|:--|
| ADMIN | `12345678` | Noe Munaq | `12345678` | Activo |
| EMPLEADO | `87654321` | Noe Developer | `12345678` | Activo |
| EMPLEADO | `11111111` | Maria Lopez | `12345678` | Activo |
| EMPLEADO | `22222222` | Carlos Ruiz | `12345678` | **Inactivo** |

> Con `22222222` puedes mostrar a propósito el caso de usuario inactivo: no puede entrar.

**Para crear más usuarios:** entra como admin → `/admin/usuarios` → botón de crear.

**Para ver los ids (los que usa la API):**

```sql
SELECT id, dni, nombre_completo, rol, estado FROM usuario;
```

---

## 3. Qué puede hacer el proyecto

### 3.1 Entrar y salir

1. En `http://localhost:4200` escribe tu **DNI** y la contraseña.
2. Clic en **Ingresar** → entras a Inicio.
3. Arriba a la derecha, tu nombre abre el panel de **Mi cuenta**: ver tus datos y **cerrar sesión**.

### 3.2 Como ADMIN

| # | Qué hacer | Dónde |
|:--|:--|:--|
| 1 | Ver el **dashboard**: ventas de hoy, productos activos, alertas de stock crítico | `/dashboard` |
| 2 | **Inventario**: ver insumos, buscar, filtrar por categoría | `/admin/inventario` |
| 3 | **Reabastecer** un insumo (ingresa cantidad y se suma al stock) | `/admin/inventario` |
| 4 | **Usuarios**: listar todos, crear empleado, activar/desactivar | `/admin/usuarios` |
| 5 | **Cambiar el rol** de un usuario (EMPLEADO ⇄ ADMIN) con el selector de cada fila | `/admin/usuarios` |
| 6 | **Proveedores**: ver, crear, editar y eliminar | desde el menú |
| 7 | **Reportes**: ventas, valorización y ranking de bebidas | `/admin/reportes` |
| 8 | **Kardex**: movimientos de inventario, con **descargar CSV** | `/admin/kardex` |
| 9 | Ver todas las **ventas** y cambiar su estado | `/ventas` |

> **Dato importante:** `/ventas` y `/admin/kardex` **no están en el menú**. Entra escribiendo la URL.

### 3.3 Como EMPLEADO

| # | Qué hacer | Dónde |
|:--|:--|:--|
| 1 | Ver el **menú** de bebidas con precio y tiempo de preparación | `/menu` |
| 2 | Ver la **receta** de una bebida (qué insumos usa y cuánto) | en `/menu` |
| 3 | **Punto de venta (POS)**: agregar bebidas al carrito y ver el total | `/menu` |
| 4 | **Cobrar**: ingresas el monto recibido y calcula el **vuelto** solo | en el POS |
| 5 | Validación: si el monto es **menor** al total, sale error y **no se registra** | en el POS |
| 6 | **Órdenes**: historial de ventas y detalle de cada una | `/ordenes` |
| 7 | **Fidelidad**: registrar la visita de un cliente | en el POS |

> Al pagar una venta, el sistema **descuenta los insumos del inventario** y deja el registro en el kardex.

---

## 4. Swagger — paso a paso

- **UI:** http://localhost:8080/swagger-ui/index.html
- **Spec en JSON:** http://localhost:8080/v3/api-docs

Sin escribir nada a mano, Swagger lista **14 controllers** y **35 rutas** generadas del código.

### Prueba 1 — El login responde desde "Try it out"

1. Abre **`auth-controller`** → **`POST /api/auth/login`**.
2. Clic en **Try it out**.
3. Selecciona todo dentro de la caja de texto y pega el JSON.
4. Clic en **Execute**.

Pega esto (login de ADMIN):

```json
{
  "dni": "12345678",
  "password": "12345678"
}
```

Debe salir **200** con:

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tipo": "Bearer",
  "idUsuario": 1,
  "dni": "12345678",
  "nombreCompleto": "Noe Munaq",
  "email": "noe.munaq@munaqfit.com",
  "rol": "ADMIN",
  "expiraEn": 86400000
}
```

**Copia el `token`** para la prueba 2.

### Prueba 2 — Un endpoint protegido responde con token

1. Arriba a la derecha, clic en **Authorize**.
2. Pega el token → clic en **Authorize** → **Close**.
3. Abre **`admin-dashboard-controller`** → **`GET /api/admin/dashboard/metricas`**.
4. **Try it out** → **Execute**.

Debe salir **200** con las métricas del dashboard.

### Prueba 3 — La seguridad sigue intacta

Cierra sesión en el Authorize (o recarga la página) y repite el paso 3 de la Prueba 2.

Debe salir **401**:

```json
{
  "message": "No autenticado: falta el token o el token expiro",
  "status": 401
}
```

Ese contraste (200 con token / 401 sin token) demuestra que abrir la documentación no rompió la seguridad.

---

## 5. Swagger — todos los bloques para pegar

> ⚠️ **Modifica datos:** los marcados con ⚠️. Hazlos al final, después de las pruebas de solo lectura.

### 5.1 Autenticación (no modifica nada)

**`POST /api/auth/login` — ADMIN → 200**

```json
{
  "dni": "12345678",
  "password": "12345678"
}
```

**`POST /api/auth/login` — EMPLEADO → 200**

```json
{
  "dni": "87654321",
  "password": "12345678"
}
```

Sale `nombreCompleto: "Noe Developer"` y `rol: "EMPLEADO"`.

**`POST /api/auth/login` — usuario INACTIVO → 401**

```json
{
  "dni": "22222222",
  "password": "12345678"
}
```

**`POST /api/auth/login` — contraseña incorrecta → 401**

```json
{
  "dni": "12345678",
  "password": "incorrecta"
}
```

**`POST /api/auth/forgot-password` → 200**

```json
{
  "email": "noe.munaq@munaqfit.com"
}
```

**`POST /api/auth/reset-password` → 200**

```json
{
  "token": "PEGA_AQUI_EL_TOKEN_DEVUELTO_EN_EL_FORGOT",
  "newPassword": "12345678"
}
```

**`GET /api/auth/validar-token`** — sin cuerpo. Solo **Try it out** → **Execute**. Devuelve 200 si el token es válido y 401 si no lo es. Es el que usa el frontend al abrir la app.

### 5.2 Solo lectura (no modifican nada)

**`GET /api/admin/dashboard/metricas`** — sin cuerpo, requiere token.

**`GET /api/empleado/menu/bebidas`** — sin cuerpo, devuelve las 12 bebidas.

**`GET /api/empleado/menu/receta/{bebidaId}`** — en el campo `bebidaId` pon `1`.

**`GET /api/productos`** — sin cuerpo.

**`GET /api/productos/buscar`** — en el campo `nombre` pon `pro`.

**`GET /api/admin/inventario/bajo`** y **`/critico`** — sin cuerpo.

**`GET /api/admin/usuarios`** — sin cuerpo, devuelve los 5 usuarios con su id.

**`GET /api/admin/ventas`** — sin cuerpo.

**`GET /api/empleado/ordenes/historial`** — sin cuerpo.

**`POST /api/empleado/ventas/vuelto`** (no registra nada, solo simula)

```json
{
  "total": 56.64,
  "recibido": 100
}
```

→ 200 con `vuelto: 43.36`

### 5.3 Reportes y kardex (se llenan en los campos, no en el cuerpo)

**`GET /api/admin/reportes/ventas`**, **`/ranking`** — requiere 2 fechas:

| Campo | Valor |
|:--|:--|
| `inicio` | `2026-10-01T00:00:00` |
| `fin` | `2026-10-02T00:00:00` |

**`GET /api/admin/reportes/valorizacion`** — sin parámetros.

**`GET /api/admin/reportes/kardex`** — todos los filtros son opcionales:

| Campo | Valor de ejemplo |
|:--|:--|
| `fechaDesde` | `2026-10-01T00:00:00` |
| `fechaHasta` | `2026-10-02T00:00:00` |
| `productoId` | `3` |
| `tipo` | `SALIDA` (o `INGRESO`) |
| `usuarioId` | *(vacío)* |

**`GET /api/admin/reportes/kardex/csv`** — mismos filtros; **descarga el CSV**.

### 5.4 Operaciones que modifican datos ⚠️

**`PUT /api/admin/usuarios/{id}/rol`** — `{id}` es el id numérico del usuario (miralo con el SQL de la sección 6).

Promover a ADMIN:

```json
{
  "rol": "ADMIN"
}
```

→ 200 con `{ "message": "Rol de Noe Developer actualizado a ADMIN" }`

**Las 3 validaciones que puedes demostrar:**

| Qué haces | Respuesta |
|:--|:--|
| `{ "rol": "JEFE" }` | **400** — `"Rol inválido. Usa ADMIN o EMPLEADO."` |
| Cambiar tu **propio** rol (id del admin) | **400** — `"No puedes quitarte tu propio rol de administrador."` |
| Un `{id}` que no existe | **404** — usuario no encontrado |

**`POST /api/admin/usuarios`** — crear empleado:

```json
{
  "nombreCompleto": "Empleado Demo",
  "dni": "55555555",
  "email": "demo@munaqfit.com",
  "password": "12345678"
}
```

**`POST /api/admin/inventario/reabastecer`**

```json
{
  "productoId": 3,
  "cantidad": 10,
  "motivo": "Compra mensual",
  "comprobante": "F001-0025",
  "proveedorId": 2,
  "fechaCaducidad": "2026-12-31"
}
```

Deja entrada en el **kardex** con `tipo_movimiento = INGRESO`.

**`POST /api/admin/proveedores`**

```json
{
  "nombre": "Proveedor de Prueba",
  "ruc": "20999999999",
  "telefono": "955000111",
  "direccion": "Av. Siempre Viva 742",
  "contactoNombre": "Persona Prueba",
  "estado": "ACTIVO",
  "tipoContrato": "FIJO"
}
```

**`POST /api/productos`**

```json
{
  "nombre": "Insumo Prueba",
  "categoriaId": 1,
  "stockActual": 25,
  "stockMinimo": 5,
  "stockCritico": 2,
  "costoUnitario": 3.50,
  "unidadMedida": "UNIDAD",
  "proveedores": [
    {
      "proveedorId": 2,
      "precioUnitario": 3.50,
      "esPrincipal": true
    }
  ]
}
```

### 5.5 La venta completa en el POS ⚠️

Son **dos pasos**: primero se registra el pedido (y se descuenta el stock), después se cobra.

**Paso 1 — `POST /api/empleado/registrar` la venta**

`POST /api/empleado/ventas/registrar`

```json
{
  "venta": {
    "mesa": 5,
    "tipoVenta": "LOCAL",
    "notas": "Prueba desde Swagger"
  },
  "detalles": [
    { "bebida": { "id": 1 }, "cantidad": 2 },
    { "bebida": { "id": 7 }, "cantidad": 1 }
  ]
}
```

> El empleado sale del **token** (tú), no del cuerpo. El `numeroPedido` lo genera el backend.

**Paso 2 — `POST /api/empleado/ventas/pago`**

```json
{
  "ventaId": 3,
  "montoPagado": 100,
  "tipoPago": "EFECTIVO"
}
```

→ 200 con el total, el vuelto y la venta ya en estado `PAGADO`.

**Con pago digital** (Yape):

```json
{
  "ventaId": 3,
  "montoPagado": 56.64,
  "tipoPago": "YAPE",
  "numeroOperacion": "987654321"
}
```

**Monto insuficiente → 400:**

```json
{
  "ventaId": 3,
  "montoPagado": 5,
  "tipoPago": "EFECTIVO"
}
```

→ 400 con `"El monto recibido es menor al total a pagar"`.

---

## 6. SQL para ver la base de datos

Puedes correr esto en **MySQL Workbench**, **DBeaver**, o en la terminal:

```bash
mysql -u root -proot123 munaqfit
```

Empieza siempre con:

```sql
USE munaqfit;
```

### 6.1 Usuarios y parámetros

```sql
SELECT id, dni, nombre_completo, email, rol, estado, fecha_creacion FROM usuario;

SELECT * FROM parametro;
```

### 6.2 Productos e inventario

```sql
SELECT id, nombre, stock_actual, stock_minimo, stock_critico, unidad_medida, costo_unitario
FROM producto ORDER BY nombre;
```

**Stock por debajo del mínimo (lo que ve la alerta):**

```sql
SELECT nombre, stock_actual, stock_minimo
FROM producto
WHERE stock_actual <= stock_minimo
ORDER BY stock_actual;
```

**Stock crítico:**

```sql
SELECT nombre, stock_actual, stock_critico
FROM producto
WHERE stock_actual <= stock_critico;
```

### 6.3 Proveedores

```sql
SELECT * FROM proveedor;
```

**Cada insumo con su proveedor y precio:**

```sql
SELECT pr.nombre AS insumo, pv.nombre AS proveedor, pp.precio_unitario, pp.es_principal
FROM producto_proveedor pp
JOIN producto pr  ON pr.id = pp.producto_id
JOIN proveedor pv ON pv.id = pp.proveedor_id
ORDER BY pr.nombre;
```

### 6.4 Bebidas y recetas

```sql
SELECT b.id, b.nombre, b.precio, cb.nombre AS categoria, b.tiempo_preparacion, b.activo
FROM bebida b
JOIN categoria_bebida cb ON cb.id = b.categoria_id
ORDER BY b.id;
```

**Las recetas completas:**

```sql
SELECT b.nombre AS bebida, p.nombre AS insumo, r.cantidad, r.paso_instruccion
FROM receta r
JOIN bebida  b ON b.id = r.bebida_id
JOIN producto p ON p.id = r.producto_id
ORDER BY b.id;
```

### 6.5 Ventas, detalle y pagos

```sql
SELECT v.id, v.numero_pedido, v.fecha_hora, v.mesa, v.tipo_venta,
       v.subtotal, v.igv, v.total, v.estado, u.nombre_completo AS empleado
FROM venta v
JOIN usuario u ON u.id = v.usuario_id
ORDER BY v.id DESC;
```

**Qué se vendió en cada pedido:**

```sql
SELECT v.numero_pedido, b.nombre AS bebida, d.cantidad, d.precio_unitario, d.subtotal
FROM detalle_venta d
JOIN venta  v ON v.id = d.venta_id
JOIN bebida b ON b.id = d.bebida_id
ORDER BY d.id;
```

**Los pagos:**

```sql
SELECT pg.id, v.numero_pedido, pg.monto_pagado, pg.monto_cambio,
       pg.tipo_pago, pg.numero_operacion, pg.estado, pg.fecha_pago
FROM pago pg
JOIN venta v ON v.id = pg.venta_id
ORDER BY pg.id DESC;
```

### 6.6 Kardex (movimientos de inventario)

```sql
SELECT m.id, p.nombre AS insumo, m.tipo_movimiento, m.cantidad,
       m.stock_anterior, m.stock_nuevo, m.motivo, m.tipo_referencia,
       m.fecha_movimiento, u.nombre_completo AS usuario
FROM movimiento_inventario m
JOIN producto p ON p.id = m.producto_id
LEFT JOIN usuario u ON u.id = m.usuario_id
ORDER BY m.id DESC;
```

### 6.7 Fidelidad

```sql
SELECT id, dni, nombre, telefono, email, visitas, umbral_premio, ultima_visita
FROM cliente_fidelidad;
```

**Historial de visitas:**

```sql
SELECT c.nombre, v.fecha_visita, v.venta_id
FROM visita_cliente v
JOIN cliente_fidelidad c ON c.id = v.cliente_fidelidad_id
ORDER BY v.fecha_visita DESC;
```

### 6.8 Lo que muestra el dashboard

```sql
SELECT COUNT(*) AS ventas_hoy, COALESCE(SUM(total), 0) AS total_hoy
FROM venta
WHERE estado = 'PAGADO' AND DATE(fecha_hora) = CURDATE();
```

**Top de bebidas vendidas:**

```sql
SELECT b.nombre, SUM(d.cantidad) AS unidades, SUM(d.subtotal) AS total
FROM detalle_venta d
JOIN bebida b ON b.id = d.bebida_id
GROUP BY b.id
ORDER BY unidades DESC;
```

### 6.9 Las 2 consultas que demuestran que el pago descontó el stock

Registra una venta de **Atomic** (bebida 1) y luego mira el producto 3 (Naranja):

```sql
SELECT id, nombre, stock_actual FROM producto WHERE id = 3;
```

```sql
SELECT tipo_movimiento, cantidad, stock_anterior, stock_nuevo, motivo, tipo_referencia
FROM movimiento_inventario
WHERE producto_id = 3
ORDER BY id DESC;
```

Tiene que aparecer una fila con `tipo_movimiento = SALIDA`, `motivo` de venta, y `stock_nuevo` menor que `stock_anterior`. Ahí está la prueba de que el POS descuenta insumos.

---

## 7. Rutas de la API (35)

**Auth (5)** · `/api/auth/login` · `/logout` · `/forgot-password` · `/reset-password` · `/validar-token`

**Productos (5)** · `/api/productos` · `/{id}` · `/buscar` · `/categoria/{categoriaId}` · y el `POST`/`PUT`/`DELETE` de productos

**Admin (16)**
`/api/admin/dashboard/metricas` · `/inventario` · `/inventario/bajo` · `/inventario/critico` · `/inventario/reabastecer` · `/usuarios` · `/usuarios/{id}` · `/usuarios/{id}/rol` · `/proveedores` · `/proveedores/{id}` · `/ventas` · `/ventas/{id}/estado` · `/reportes/ventas` · `/reportes/valorizacion` · `/reportes/ranking` · `/reportes/kardex` · `/reportes/kardex/csv`

**Empleado (9)**
`/api/empleado/menu/bebidas` · `/menu/receta/{bebidaId}` · `/ventas/registrar` · `/ventas/pago` · `/ventas/vuelto` · `/ordenes/historial` · `/ordenes/{ventaId}/detalle` · `/fidelidad/registrar` · `/fidelidad/{clienteId}/visita`

---

## 8. Problemas frecuentes

| Síntoma | Causa y solución |
|:--|:--|
| `401` en Swagger | No pegaste el token. Haz **Authorize** primero. |
| `401` al entrar a la app | DNI o contraseña mal escritos. |
| `401` con el DNI `22222222` | Usuario **INACTIVO** a propósito. |
| `El monto recibido es menor al total a pagar` | Validación del POS: cobra el monto completo. |
| `Communications link failure` | MySQL está apagado, o las credenciales en `application.properties` no coinciden. |
| Las tablas están vacías | Corriste `seed.sql` **antes** de que Flyway creara las tablas. |
| `Unknown database 'munaqfit'` | No creaste la base. Revisa el paso 1 de la sección 1. |
| Puerto 8080 u 4200 ocupado | Cierra el proceso que lo usa, o cambia el puerto. |
| La app abre pero no carga datos | El backend no está corriendo en el puerto 8080. |
| Olvidé qué endpoint es | Están los 35 listados en la sección 7, y en `/v3/api-docs`. |

---

## 9. Nota para la demostración en línea (cuando desplegues)

La documentación de Swagger viene **dentro del backend**, así que en el servidor queda automáticamente en:

- **UI:** `https://<tu-backend>.onrender.com/swagger-ui/index.html`
- **JSON:** `https://<tu-backend>.onrender.com/v3/api-docs`

Solo cambia `localhost:8080` por el dominio. **No hay que hacer nada extra** en el despliegue, y todo lo de esta guía se prueba igual.

Si quieres ocultarla en producción sin redesplegar, define `SWAGGER_ENABLED=false` en las variables de entorno del servidor.
