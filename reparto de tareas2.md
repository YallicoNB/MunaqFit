# Reparto de Tareas 2 - MunaqFit Manager

> **Segunda etapa del proyecto.** El MVP funcional ya está construido, integrado y
> auditado (ver `reparto de tareas.md`). Este documento reparte lo que **falta**
> para que el sistema cumpla el 100% de la especificación técnica recibida.
>
> Todo lo que aparece aquí sale de **código**, no de documentación.

---

## Estado al inicio de esta iteración

**Developer 1 entregó el 100% de su bloque (1.1 – 1.12), todo verificado en vivo,
junto con los préstamos de Dev 3 que lo desbloqueaban (3.1, 3.5 y 3.6).** El
backend quedó migrado con Flyway V1–V4, la relación N-N funciona, el POS cobra
y el 409 de concurrencia responde correctamente. Commits de referencia:

| Commit | Qué entrega |
|:---|:---|
| `0fa4469` | 3.1 Bootstrap 5 · 3.5 servicio de Toast · 3.6 `<app-toast>` |
| `bb41c79` | 1.1–1.5 Flyway (V1/V2) + tabla puente `producto_proveedor` + `ProductoResponseDTO` |
| `d5a5b03` | 1.10–1.11 Concurrencia (`@Version` → 409) y formato `PED-{fecha}-{secuencia}` · 1.12 `V3` columnas nutricionales · **`V4`** bloqueo optimista |
| `f5d2c2a` | 1.6–1.9 POS de ventas con carrito (Signals), modal de cobro y link en el navbar |

> Nada fue pusheado: los cuatro commits viven en la rama local `feat/dev1-etapa-2`.
> **Dev 2, Dev 3 y Dev 4 arrancan sin esperar nada de Dev 1.** Solo deben respetar
> la propiedad de archivos y la regla de maquetado con `shared/` (ver abajo).

---

## ¿Por qué empezar por el esquema de datos?

| Razón | Explicación |
|:---|:---|
| **1. Es el bloqueador** | La relación N-N y las migraciones cambian entidades Java. Si se empieza por las pantallas, hay que rehacerlas dos veces |
| **2. Rompe el `DROP DATABASE`** | Hoy `schema.sql` borra todo al ejecutarse. Sin Flyway, el día que se agregue la N-N se pierde el trabajo anterior |
| **3. Marca el contrato del backend** | La estructura de `producto_proveedor` define qué DTOs devuelve el CRUD de productos que develops Dev 4 |
| **4. Los datos actuales se migran una vez** | Pasarlos a la tabla puente es un paso único. Después solo se crean relaciones nuevas |

---


---

## Auditoría de brechas: qué requisito sigue sin cumplirse

> Resueltos por Dev 1 en esta iteración: **#3** (concurrencia RNF-008),
> **#4** (pantalla de Ventas REQ-010), **#7** (Bootstrap RNF-009) y **#10** (Toast RNF-015).

| # | Requisito | Dónde lo pide | Estado actual | Developer |
|:---|:---|:---|:---|:---|
| 1 | Relación **muchos a muchos** | 3.3 (nota del profe) | Los 13 FK son 1-N o 1-1. `receta` parece puente pero tiene atributos (`cantidad`), así que es entidad asociativa | **1** |
| 2 | **Migraciones** de base de datos | 3.3 | Solo hay `schema.sql` con `DROP DATABASE`. Cada cambio de esquema borra todo | **1** |
| 3 | **Concurrencia** entre empleados | RNF-008 | Stock y número de pedido sin protección ante escrituras simultáneas | **1** |
| 4 | **Pantalla de Ventas** (tomar pedido) | REQ-010/012/013 · **ALTA** | El backend está completo pero **no existe la pantalla**. `/ventas` es un stub | **1** |
| 5 | **Reporte Kardex** | REQ-022 | `AdminReportesController` solo tiene ranking, ventas y valorización | **2** |
| 6 | **Exportar Kardex** a CSV/Excel | REQ-022.4 | No existe | **2** |
| 7 | **Bootstrap 5+** | RNF-009 | No está en `package.json` ni en `angular.json` | **3** |
| 8 | **Diseño responsivo** (4 tamaños) | RNF-013 | Cero `@media` en todo el proyecto | **3** |
| 9 | **Componentes reutilizables** | 3.7 | Solo `sidebar` tiene `@Input`/`@Output` | **3** |
| 10 | **Toast / feedback visual** | RNF-015 | No existe servicio de notificaciones | **3** |
| 11 | **CRUD completo + DTOs** | 3.8 | `ProductoController` es solo lectura y devuelve entidades JPA. `usuarios` no tiene `PUT` | **4** |
| 12 | **Accesibilidad** | 3.7 | 3 atributos `alt`, cero `aria-`, cero `role=`, cero `<label>` | **4** |
| 13 | **Formularios validados (cliente)** | 3.7 | Todo es `FormsModule` template-driven, sin validadores | **4** |

> Ya cumplido y **no se toca**: contraseñas encriptadas (BCrypt, REQ-002),
> relaciones 1-N (13 FK), API completa de ventas/pagos/inventario/fidelidad,
> 33 `CHECK`, 10 `UNIQUE`, errores 401/404/405 en vez de 500.

---

## División de Tareas por Developers (4 Integrantes)

---

### Developer 1 (Noe) — Núcleo del Negocio + Modelo de Datos — ✅ COMPLETO (1.1 – 1.12)

**Objetivo:** Cerrar el flujo que hace que el sistema realmente venda, y modelar los datos correctamente. Es la parte más difícil y la más visible en la sustentación.

> **Estado: bloque entregado íntegro** (`bb41c79`, `d5a5b03`, `f5d2c2a`). No re-hagas
> ninguna tarea de esta tabla; si necesitas ajustar algo, avisa a Dev 1 (los archivos
> conservan su propiedad).
>
> **Notas de implementación (deltas menores del reparto, sin impacto funcional):**
> - 1.7: el carrito con Signals vive dentro de `pages/ventas/ventas.ts` (no hay
>   `service/carrito.service.ts`) y el IGV de vista previa es una constante `0.18`;
>   el total definitivo lo calcula el backend leyendo el parámetro `IGV` de la BD.
> - 1.8: el modal cobra EFECTIVO con vuelto automático y pagos digitales por el monto
>   exacto; en Transferencia todavía no pide N° de operación/banco (el modelo
>   `PagoRequest` ya lo soporta).

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 1.1 | Crear `V1__baseline.sql` | `backend/src/main/resources/db/migration/V1__baseline.sql` | Copia del esquema actual de 14 tablas, **sin** `DROP DATABASE`. Se agrega la dependencia `flyway-core` al `pom.xml` y se activa con `spring.flyway.enabled=true` |
| 1.2 | Migrar a Flyway | `backend/src/main/resources/application.properties` | Cambiar `ddl-auto` de `none` a **`validate`**. A partir de ahí Spring falla al arrancar si el código y la base de datos no coinciden |
| 1.3 | Tabla puente N-N | `database/schema.sql`, `V2__n_n_proveedor.sql` | Nueva tabla `producto_proveedor (producto_id, proveedor_id, precio_unitario, es_principal)` con PK compuesta y 2 FK. `V2` copia los datos de `producto.proveedor_id` y luego elimina esa columna |
| 1.4 | Entidad y repositorio N-N | `model/ProductoProveedor.java`, `repository/ProductoProveedorRepository.java` | Entidad con `@EmbeddedId` o clase de clave compuesta, y repositorio JPA |
| 1.5 | `Producto` con varios proveedores | `model/Producto.java` | Reemplazar el `ManyToOne` a `Proveedor` por un `OneToMany` a `ProductoProveedor`. Crear `dto/ProductoResponseDTO.java` con la lista de proveedores y su precio |
| 1.6 | Pantalla de Ventas (POS) | `pages/ventas/ventas.ts`, `ventas.html`, `ventas.css` | Reemplazar el stub por la pantalla de REQ-010: panel izquierdo con el catálogo agrupado por las 8 categorías, panel derecho con el carrito. Buscador, clic para agregar, `+`/`-`, eliminar, notas del pedido, número de mesa |
| 1.7 | Carrito con Signals | `service/carrito.service.ts` (nuevo) | Estado del carrito con `signal` y `computed`: lista de líneas, cantidades, subtotal, IGV (leído de `parametro`) y total en tiempo real. Cubre 3.7 "Gestión de estado" |
| 1.8 | Modal de pago | `pages/ventas/ventas.html` | REQ-012: Efectivo (monto recibido → vuelto automático), Yape, Plin, Transferencia (nº de operación + banco) y QR. Confirma con `POST /api/empleado/ventas/registrar` y luego `/pago` o `/vuelto` |
| 1.9 | Link del navbar | `layouts/navbar/navbar.html` | Hoy el link a `/ventas` es `*ngIf="esAdmin"`, pero REQ-010 es de **EMPLEADO**. Debe verse para cualquier usuario autenticado |
| 1.10 | Concurrencia | `model/Producto.java`, `model/Venta.java`, `exception/GlobalExceptionHandler.java` | `@Version` (optimistic locking) en Producto y Venta, y captura de `OptimisticLockingFailureException` → **409**. Cubre RNF-008 |
| 1.11 | Formato del número de pedido | `service/VentaService.java` | REQ-012.7 pide `PED-{fecha}-{secuencia}`; hoy genera `PED-000001`. Ajustar a `PED-20260928-0001` |
| 1.12 | Columnas nutricionales | `V3__datos_nutricionales.sql` | REQ-018: calorías, proteínas, carbohidratos, grasas, fibra y azúcares en `bebida`. Los **datos y el render** los hace Dev 4 |

---

### Developer 2 — Kardex y Reportes de Auditoría

**Objetivo:** Implementar el reporte Kardex completo, que es el requisito que el docente nombró explícitamente.

> Puedes arrancar ya, sin esperar nada. Tu pantalla (2.6) **maqueta con los
> componentes `shared/` de Dev 3** (Tabla, Boton, EstadoVacio, Spinner): si no
> existen todavía, primero haces el backend (2.1–2.5) y consumes `shared/` cuando
> llegue, o le pides a Dev 3 los que necesites. No dupliques markup a mano.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 2.1 | Servicio de Kardex | `service/ReporteKardexService.java` (nuevo) | Consulta de `movimiento_inventario` con los 4 filtros de REQ-022.3: rango de fechas, insumo, tipo de movimiento y usuario |
| 2.2 | Query parametrizado | `repository/MovimientoInventarioRepository.java` | `@Query` con `#{...}` o `:nombre`. **Nunca concatenar SQL.** La consulta debe traer las 8 columnas que pide REQ-022.2: fecha/hora, insumo, tipo, cantidad, stock anterior, stock nuevo, motivo y usuario |
| 2.3 | DTO de respuesta | `dto/KardexFilaDTO.java` (nuevo) | Record con los 8 campos del reporte. **Devolver DTOs, no la entidad `MovimientoInventario`**, que arrastra la relación con `Producto` y `Usuario` |
| 2.4 | Endpoint del reporte | `controller/ReporteKardexController.java` (nuevo) | `GET /api/admin/reportes/kardex?fechaDesde=&fechaHasta=&productoId=&tipo=&usuarioId=`. Solo rol ADMIN |
| 2.5 | Exportación a CSV | `controller/ReporteKardexController.java` | `GET /api/admin/reportes/kardex/csv` con los mismos filtros. `Content-Type: text/csv`, `Content-Disposition: attachment`, separador `;` (así lo abre Excel en español) y BOM UTF-8 para los acentos |
| 2.6 | Pantalla del Kardex | `pages/admin/kardex/*` (nuevo) | Ruta `/admin/kardex`. Tabla con los 8 campos, panel de filtros, botón "Descargar CSV" y estados de carga / vacío / error — **con los componentes `shared/` de Dev 3** |
| 2.7 | Cliente HTTP y ruta | `service/reporte.service.ts`, `app.routes.ts` | Agregar los 2 métodos al servicio (ya existe el archivo; solo agrega los métodos del Kardex) y la ruta protegida con `adminGuard`. Link en el sidebar de admin |

---

### Developer 3 — Base Visual y Componentes Reutilizables

**Objetivo:** Sentar las bases de UI que los demás developers reutilizan. Es lo primero que hay que entregar porque bloquea a los otros tres.

> **Ya entregado por Dev 1 (préstamo):** 3.1 (Bootstrap 5), 3.5 (servicio de Toast)
> y 3.6 (`<app-toast>` en `app/shared/toast/`) — **no se vuelven a hacer.**
> Tu trabajo restante es **3.2, 3.3, 3.4 y 3.7**.
>
> **Regla:** construyes los componentes de `shared/` para que Dev 2 y Dev 4 los
> consuman — **no maquetes a mano las vistas de ellos**, y `pages/ventas/*` es
> propiedad de Dev 1 (POS ya entregado): pídelo antes de migrarlo en 3.7.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 3.1 | Instalar Bootstrap 5 | `package.json`, `angular.json` | ~~`npm i bootstrap@5 @popperjs/core`, y registrar los CSS en el arreglo `styles` de `angular.json`. Cubre RNF-009~~ — ✅ entregado por Dev 1 (`0fa4469`) |
| 3.2 | Diseño responsivo | `src/styles.css`, CSS de las 15 vistas | `@media` para los 4 tamaños de RNF-013: 1920×1080, 1366×768, 768×1024 y **375×667 (móvil)**. Hoy no hay un solo `@media` en el proyecto |
| 3.3 | Grid y utilidades | CSS de las vistas | Reemplazar los CSS a mano por clases de Bootstrap (grid, flex, espaciado, tarjetas). Las vistas de Dev 2 y Dev 4 las maquetan **ellos** con tus componentes; coordina para no pisarte |
| 3.4 | Componentes reutilizables | `app/shared/*` (nuevo) | `Tabla`, `Modal`, `Boton`, `Input`, `Badge`, `Card`, `EstadoVacio` y `Spinner`. Todos con `@Input`/`@Output`, `selector` propio y `aria-`/`<label>`/`role` incorporados (le ahorra accesibilidad a Dev 4). **Es el entregable que consumen Dev 2 y Dev 4** |
| 3.5 | Servicio de Toast | `service/toast.service.ts` (nuevo) | ~~`exito()` / `error()` / `info()` / `warning()` con un `signal` para la cola de mensajes~~ — ✅ entregado por Dev 1 (`0fa4469`) |
| 3.6 | Componente `<app-toast>` | `app/shared/toast/*` (nuevo) | ~~Renderiza la cola en una esquina con `aria-live="polite"`. Declararlo una vez en `app.ts`~~ — ✅ entregado por Dev 1 (`0fa4469`) |
| 3.7 | Migrar vistas a `shared/` | Vistas existentes | Reemplazar HTML repetido (tarjetas de tabla, botones, spinners) por los componentes nuevos. Consultar a Dev 1 antes de tocar `pages/ventas/*` y a Dev 4 antes de sus vistas en curso |

---

### Developer 4 — CRUD Faltante, Accesibilidad y Formularios

**Objetivo:** Cerrar los huecos del backend, el diseño accesible y los formularios validados. Todos los items son mecánicos y de bajo riesgo.

> **Regla de maquetado (importante):** las tareas **4.4, 4.5 y 4.8 se construyen
> sobre los componentes `shared/` de Dev 3** (Tabla, Modal, Boton, Input, Badge,
> Card, EstadoVacio, Spinner) — **no se maqueta a mano**. Si un componente no
> existe aún, se lo pides a Dev 3; no lo duplicas. El arranque de esas tres espera
> la entrega de 3.4/3.7 de Dev 3; las demás (4.1, 4.2, 4.3, 4.6, 4.7) no esperan a nadie.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 4.1 | DTOs de producto | `dto/ProductoRequestDTO.java` (nuevo), `dto/ProductoResponseDTO.java` | `ProductoResponseDTO` **ya lo creó Dev 1 en 1.5** (`bb41c79`) — úsalo tal cual. El de request sí es de Dev 4 |
| 4.2 | CRUD de productos | `controller/ProductoController.java` | Hoy es **solo lectura**. Agregar `POST /api/productos`, `PUT /api/productos/{id}` y `DELETE /api/productos/{id}`, con `@Valid` y errores claros, devolviendo `ProductoResponseDTO` |
| 4.3 | Actualizar usuario | `controller/AdminUsuarioController.java` | Falta `PUT /api/admin/usuarios/{id}`. Validar que el DNI no se duplique |
| 4.4 | Accesibilidad | Componentes `shared/` de Dev 3 + restos de las vistas | `aria-label` en botones de solo icono, `role` en nav/tabla/modal, `<label>` real en cada input, foco visible, contraste AA y `aria-live` en mensajes de error — **dentro de los componentes de Dev 3**, para que llegue a todas las vistas de una vez |
| 4.5 | Formularios reactivos | `pages/auth/login/*`, `pages/admin/usuarios/*`, `pages/admin/inventario/*`, `pages/admin/reportes/*` | Migrar de `FormsModule` a `ReactiveFormsModule` con validadores: requerido, email, DNI de 8 dígitos, mínimo de contraseña. Mensajes de error en español, renderizados con **`<app-input>` de Dev 3** (no con `<input>` sueltos) |
| 4.6 | Info nutricional | `pages/menu/menu.html`, `models/producto.ts` | REQ-018: los campos `calorias`, `proteinas`, `carbohidratos`, `grasas`, `fibra`, `azucares` **ya los expone `/menu/bebidas` desde `V3`** — extiende el modelo `Bebida` y muéstralos en la vista de receta, con **"Información nutricional no disponible"** cuando no haya datos |
| 4.7 | Links muertos del navbar | `layouts/navbar/navbar.html` | Quitar **solo** el link a `/empleados` (página stub que duplica `pages/admin/usuarios/`). El link "Punto de venta" ya lo puso Dev 1 (1.9, `f5d2c2a`) — **no lo toques** |
| 4.8 | Estados consistentes | Todas las vistas | Unificar `cargando` / `error` / `vacío` usando `EstadoVacio`, `Spinner` y `Badge` de Dev 3 — no CSS suelto por vista |

---

## Dependencias y orden de ejecución

| Dependencia | Quién depende | Qué necesita |
|:---|:---|:---|
| 3.1 Bootstrap | Devs 1, 2, 4 | ~~Las clases de Bootstrap para maquetar~~ — ✅ entregado (Dev 1, `0fa4469`) |
| 3.4 `shared/` | Devs 2, 4 | Tabla, modal, botón, input, toast. **Dev 4 no puede maquetar a mano: sus tareas 4.4/4.5/4.8 se construyen sobre estos componentes** |
| 1.3 / 1.5 N-N | Dev 4 | `ProductoResponseDTO` — ✅ ya entregado (Dev 1, `bb41c79`). Dev 4 empieza su CRUD sin esperar |
| 1.10 / 1.12 | Dev 4 | Concurrencia y columnas nutricionales — ✅ entregados (`d5a5b03`): 4.6 ya tiene los campos `V3` expuestos por `/menu/bebidas` |
| 1.1 / 1.2 Flyway | Devs 2, 3, 4 | Nadie debe tocar `database/` ni `application.properties` (hay V1–V4 aplicadas, `success=1`) |
| 3.5 Toast | Devs 1, 2, 4 | ✅ entregado (Dev 1, `0fa4469`) — no se vuelve a crear |

> **Regla de propiedad de archivos:** cada tabla de la sección anterior indica qué
> archivos puede tocar cada developer. Si necesitas un archivo de otro, avisa; no
> lo edites. Así no hay conflictos al integrar.
>
> **Regla de maquetado:** Dev 2 y Dev 4 consumen los componentes `shared/` de Dev 3;
> no se escribe markup/estilos duplicados en vistas nuevas.

---

## Estado de avance

Punto de partida de esta segunda etapa: todo lo siguiente estaba pendiente.
**Estado actualizado tras la entrega de Dev 1:**

| Developer | Tarea | Estado |
|:---|:---|:---|
| **Dev 1** | 1.1 – 1.5 Migraciones Flyway + relación N-N `producto ↔ proveedor` | ✅ Completado (`bb41c79`) |
| **Dev 1** | 1.6 – 1.9 Pantalla de Ventas (POS) con Signals | ✅ Completado (`f5d2c2a`) |
| **Dev 1** | 1.10 – 1.11 Concurrencia (`@Version`) y formato `PED-{fecha}-{secuencia}` | ✅ Completado (`d5a5b03`) |
| **Dev 1** | 1.12 Columnas nutricionales en `V3` | ✅ Completado (`d5a5b03`) |
| **Dev 2** | 2.1 – 2.5 Reporte Kardex con filtros y export CSV | Pendiente |
| **Dev 2** | 2.6 – 2.7 Pantalla `/admin/kardex` | Pendiente (maqueta con `shared/` de Dev 3) |
| **Dev 3** | 3.1 Bootstrap 5 | ✅ Completado por Dev 1 (`0fa4469`) |
| **Dev 3** | 3.2 – 3.3 Diseño responsivo y grid/utilidades | Pendiente |
| **Dev 3** | 3.4 Componentes `shared/` (Tabla, Modal, Boton, Input, Badge, Card, EstadoVacio, Spinner) | Pendiente — **bloquea a Dev 2 y Dev 4** |
| **Dev 3** | 3.5 – 3.6 Servicio de Toast y `<app-toast>` | ✅ Completado por Dev 1 (`0fa4469`) |
| **Dev 3** | 3.7 Migración de vistas a `shared/` | Pendiente |
| **Dev 4** | 4.1 – 4.3 DTOs, CRUD de productos y `PUT` de usuarios | Pendiente (dependencia 1.5 ya resuelta) |
| **Dev 4** | 4.4 Accesibilidad | Pendiente — sobre los componentes `shared/` de Dev 3 |
| **Dev 4** | 4.5 – 4.6 Formularios reactivos e info nutricional | Pendiente (4.5 usa `<app-input>` de Dev 3; 4.6 ya tiene la `V3`) |
| **Dev 4** | 4.7 – 4.8 Links muertos y estados consistentes | Pendiente (4.8 usa `EstadoVacio`/`Spinner`/`Badge` de Dev 3) |

### Verificación de cierre

| Comprobación | Comando | Resultado esperado |
|:---|:---|:---|
| Backend compila | `mvn clean compile` en `backend/` | Sin errores |
| Frontend compila | `ng build` en `frontend/` | Sin errores ni warnings |
| Base migrada | Arrancar con Flyway activo | `V1`, `V2`, `V3` y **`V4`** aplicadas con `success=1`, datos intactos |
| N-N funciona | `SELECT * FROM producto_proveedor` | 12 filas migradas desde `producto.proveedor_id` |
| Kardex filtra | `GET /api/admin/reportes/kardex?tipo=SALIDA` | Solo salidas, con stock anterior y nuevo |
| CSV descarga | `GET /api/admin/reportes/kardex/csv` | Archivo con acentos correctos |
| POS registra | Login → `/ventas` → 2 bebidas → cobrar | Venta `PED-20260928-0001`, stock descontado, kardex con la salida |
| Concurrencia | Dos ventas simultáneas sobre el mismo insumo | La segunda devuelve **409** |
| Sustentación | ✅ Probado por Dev 1 en vivo: POS cobró `PED-0001` (EFECTIVO, vuelto 4.48) y `PED-0002` (YAPE), 409 en doble pago y en choque paralelo, stock siempre coherente | Listo para sustentar |

---

## Fuera de alcance del MVP (documentar en 1.2.3)

| Requisito | Motivo |
|:---|:---|
| REQ-027 · Pedidos a proveedores | Es un módulo completo: tabla nueva, CRUD y estados. No cabe sin romper el cronograma |
| REQ-028 · Control de entregas (peso bruto/neto) | Depende directamente de REQ-027 |
| REQ-009 · Cambio de contraseña auto-gestionado | Requiere envío de correo, y el proyecto no incluye SMTP |

> Los tres quedan documentados como *no implementado en el MVP*, con el motivo.

---

## Reglas de negocio que se mantienen

No cambian, pero se escribe más código, así que conviene tenerlas presentes:

- El empleado que registra la venta se toma del **token JWT**, nunca del cuerpo del request.
- El IGV sale de la tabla `parametro` (**18%**), no de una constante en el código.
- Cada venta descuenta stock **según la receta** de la bebida, en la unidad canónica de
  `producto.unidad_medida`, y deja un movimiento `SALIDA` con `tipo_referencia = 'VENTA'`.
- Si un insumo queda sin stock, la venta se rechaza **completa**: no se guarda nada a medias.
- No se puede pagar dos veces la misma venta, ni con monto menor al total.
- El vuelto solo existe en efectivo. Con Yape, Plin, QR o transferencia el monto es exacto.
- La N-N **no reemplaza** estas reglas: un producto puede tener varios proveedores, pero
  sigue teniendo un solo `stock_actual` y un solo `stock_critico`.

---

## Resumen de Tareas por Developer

| Developer | Responsabilidad | Tareas | Dificultad | Depende de |
|:---|:---|:---|:---|:---|
| **Developer 1 (Noe)** | POS + modelo de datos + concurrencia | 1.1 – 1.12 | **ALTA** | — |
| **Developer 2** | Kardex y exportación | 2.1 – 2.7 | **MEDIA** | — |
| **Developer 3** | Bootstrap, `shared/`, Toast | 3.1 – 3.7 | **MEDIA-BAJA** | — |
| **Developer 4** | CRUD faltante, accesibilidad, formularios | 4.1 – 4.8 | **BAJA** | Dev 1 (1.5) · Dev 3 (3.4/3.7 para 4.4/4.5/4.8) |

> El reparto es **deliberadamente desigual**: Dev 1 carga con el flujo que hace que el
> sistema venda y con el modelado de datos, que es lo más difícil de revertir. Dev 4
> carga con más items, pero todos son mecánicos y de bajo riesgo.

---

## Archivos nuevos en esta etapa

```
backend/
└── src/main/resources/db/migration/          ← NUEVO (Dev 1) ✅ aplicadas
    ├── V1__baseline.sql                      ✅
    ├── V2__n_n_proveedor.sql                 ✅
    ├── V3__datos_nutricionales.sql           ✅
    └── V4__version_optimista.sql             ✅ (creada en 1.10)

backend/src/main/java/com/munaqfit/backend/
├── model/ProductoProveedor.java              ✅ (Dev 1)
├── repository/ProductoProveedorRepository.java  ✅ (Dev 1)
├── repository/MovimientoInventarioRepository.java  ← Dev 2 (agrega @Query)
├── service/ReporteKardexService.java         ← NUEVO (Dev 2)
├── controller/ReporteKardexController.java   ← NUEVO (Dev 2)
├── dto/KardexFilaDTO.java                    ← NUEVO (Dev 2)
├── dto/ProductoRequestDTO.java               ← NUEVO (Dev 4)
└── dto/ProductoResponseDTO.java              ✅ (Dev 1, compartido con Dev 4)

frontend/src/app/
├── pages/ventas/                             ✅ (Dev 1, POS terminado)
├── pages/admin/kardex/                       ← NUEVO (Dev 2, maquetado con shared/)
├── service/toast.service.ts                  ✅ (Dev 1, `0fa4469`)
├── shared/                                   ← Dev 3
│   ├── toast/                                ✅ (Dev 1, `0fa4469`)
│   ├── tabla/  modal/  boton/  input/        ← NUEVO (Dev 3, bloquea a Dev 2/4)
│   └── badge/  card/   estado-vacio/  spinner/  ← NUEVO (Dev 3)
└── layouts/navbar/navbar.html                ← Devs 1 y 4 (coordinar): el link
                                                "Punto de venta" es de Dev 1; Dev 4
                                                solo quita `/empleados`
```

---

## Contratos de API que fija Dev 1 y consume Dev 4

Estos nombres no se cambian sin avisar, para que el frontend y el backend no se separen.

> Todos los contratos de esta tabla ya fueron verificados en vivo por Dev 1.

| Endpoint | Método | Cuerpo | Respuesta |
|:---|:---|:---|:---|
| `/api/empleado/ventas/registrar` | `POST` | `{"venta":{...},"detalles":[{"bebida":{"id":N},"cantidad":N}]}` | `Venta` con `numero_pedido` y `total` |
| `/api/empleado/ventas/pago` | `POST` | `PagoRequest` | `{montoTotal, vuelto, ...}` |
| `/api/empleado/ventas/vuelto` | `POST` | `PagoRequest` (solo efectivo) | `{montoTotal, vuelto, ...}` |
| `/api/admin/reportes/kardex` | `GET` | query params opcionales | `KardexFilaDTO[]` |
| `/api/admin/reportes/kardex/csv` | `GET` | mismos query params | `text/csv` como descarga |

> **El frontend ya espera** `montoTotal` y `vuelto` en la respuesta del pago, y
> `categoria` (texto) en cada bebida. No se cambian.

---

## Primera Acción para Developer 1 (Noe) — ✅ Cumplida

**Estado:** los 4 pasos de abajo ya están hechos y verificados en `feat/dev1-etapa-2`.
Esta sección se conserva como registro de cómo se desbloqueó a los demás developers.

1. **Instala Bootstrap 5** y regístralo en `angular.json` (es lo que desbloquea a los
   otros tres). Si lo haces tú, Dev 3 empieza con la parte 2 de su tarea 3.1.
2. **Crea `V1__baseline.sql`** partiendo de `database/schema.sql`, quitándole el
   `DROP DATABASE` y el `CREATE DATABASE`. Verifica que la base actual se reconstruya
   idéntica antes de tocar nada más.
3. **Crea `V2__n_n_proveedor.sql`**: primero la tabla puente, luego el `INSERT ... SELECT`
   para migrar los proveedores actuales, y solo después el `ALTER TABLE` que elimina
   `producto.proveedor_id`. En ese orden, para que ningún momento queden datos huérfanos.
4. **Avisa a Dev 4** que `ProductoResponseDTO` ya está listo, para que empiece su CRUD.