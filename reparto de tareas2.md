# Reparto de Tareas 2 - MunaqFit Manager

> **Segunda etapa del proyecto.** El MVP funcional ya está construido, integrado y
> auditado (ver `reparto de tareas.md`). Este documento reparte lo que **falta**
> para que el sistema cumpla el 100% de la especificación técnica recibida.
>
> Todo lo que aparece aquí sale de **código**, no de documentación.

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

### Developer 1 (Elvis) — Núcleo del Negocio + Modelo de Datos

**Objetivo:** Cerrar el flujo que hace que el sistema realmente venda, y modelar los datos correctamente. Es la parte más difícil y la más visible en la sustentación.

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

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 2.1 | Servicio de Kardex | `service/ReporteKardexService.java` (nuevo) | Consulta de `movimiento_inventario` con los 4 filtros de REQ-022.3: rango de fechas, insumo, tipo de movimiento y usuario |
| 2.2 | Query parametrizado | `repository/MovimientoInventarioRepository.java` | `@Query` con `#{...}` o `:nombre`. **Nunca concatenar SQL.** La consulta debe traer las 8 columnas que pide REQ-022.2: fecha/hora, insumo, tipo, cantidad, stock anterior, stock nuevo, motivo y usuario |
| 2.3 | DTO de respuesta | `dto/KardexFilaDTO.java` (nuevo) | Record con los 8 campos del reporte. **Devolver DTOs, no la entidad `MovimientoInventario`**, que arrastra la relación con `Producto` y `Usuario` |
| 2.4 | Endpoint del reporte | `controller/ReporteKardexController.java` (nuevo) | `GET /api/admin/reportes/kardex?fechaDesde=&fechaHasta=&productoId=&tipo=&usuarioId=`. Solo rol ADMIN |
| 2.5 | Exportación a CSV | `controller/ReporteKardexController.java` | `GET /api/admin/reportes/kardex/csv` con los mismos filtros. `Content-Type: text/csv`, `Content-Disposition: attachment`, separador `;` (así lo abre Excel en español) y BOM UTF-8 para los acentos |
| 2.6 | Pantalla del Kardex | `pages/admin/kardex/*` (nuevo) | Ruta `/admin/kardex`. Tabla con los 8 campos, panel de filtros, botón "Descargar CSV" y estados de carga / vacío / error |
| 2.7 | Cliente HTTP y ruta | `service/reporte.service.ts`, `app.routes.ts` | Agregar los 2 métodos al servicio y la ruta protegida con `adminGuard`. Link en el sidebar de admin |

---

### Developer 3 — Base Visual y Componentes Reutilizables

**Objetivo:** Sentar las bases de UI que los demás developers reutilizan. Es lo primero que hay que entregar porque bloquea a los otros tres.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 3.1 | Instalar Bootstrap 5 | `package.json`, `angular.json` | `npm i bootstrap@5 @popperjs/core`, y registrar los CSS en el arreglo `styles` de `angular.json`. Cubre RNF-009 |
| 3.2 | Diseño responsivo | `src/styles.css`, CSS de las 15 vistas | `@media` para los 4 tamaños de RNF-013: 1920×1080, 1366×768, 768×1024 y **375×667 (móvil)**. Hoy no hay un solo `@media` en el proyecto |
| 3.3 | Grid y utilidades | CSS de las vistas | Reemplazar los CSS a mano por clases de Bootstrap (grid, flex, espaciado, tarjetas) |
| 3.4 | Componentes reutilizables | `app/shared/*` (nuevo) | `Tabla`, `Modal`, `Boton`, `Input`, `Badge`, `Card`, `EstadoVacio` y `Spinner`. Todos con `@Input`/`@Output` y `selector` propio. Cubre 3.7 "componentes reutilizables" |
| 3.5 | Servicio de Toast | `service/toast.service.ts` (nuevo) | `exito()` / `error()` / `info()` / `warning()` con un `signal` para la cola de mensajes |
| 3.6 | Componente `<app-toast>` | `app/shared/toast/*` (nuevo) | Renderiza la cola en una esquina con `aria-live="polite"`. Declararlo una vez en `app.ts` |
| 3.7 | Migrar vistas a `shared/` | Vistas existentes | Reemplazar HTML repetido (tarjetas de tabla, botones, spinners) por los componentes nuevos |

---

### Developer 4 — CRUD Faltante, Accesibilidad y Formularios

**Objetivo:** Cerrar los huecos del backend, el diseño accesible y los formularios validados. Todos los items son mecánicos y de bajo riesgo.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 4.1 | DTOs de producto | `dto/ProductoRequestDTO.java`, `dto/ProductoResponseDTO.java` (nuevo) | `ProductoResponseDTO` lo crea Dev 1 en 1.5 — **coordinarse antes de empezar**. El de request sí es de Dev 4 |
| 4.2 | CRUD de productos | `controller/ProductoController.java` | Hoy es **solo lectura**. Agregar `POST /api/productos`, `PUT /api/productos/{id}` y `DELETE /api/productos/{id}`, con `@Valid` y errores claros |
| 4.3 | Actualizar usuario | `controller/AdminUsuarioController.java` | Falta `PUT /api/admin/usuarios/{id}`. Validar que el DNI no se duplique |
| 4.4 | Accesibilidad | Todas las vistas | `aria-label` en botones de solo icono, `role` en nav/tabla/modal, `<label>` real en cada input (hoy no hay ninguno), foco visible, contraste AA y `aria-live` en mensajes de error |
| 4.5 | Formularios reactivos | `pages/auth/login/*`, `pages/admin/usuarios/*`, `pages/admin/inventario/*`, `pages/admin/reportes/*` | Migrar de `FormsModule` a `ReactiveFormsModule` con validadores: requerido, email, DNI de 8 dígitos, mínimo de contraseña. Mensajes de error en español |
| 4.6 | Info nutricional | `pages/menu/menu.html` | REQ-018: mostrar calorías, proteínas, carbohidratos, grasas, fibra y azúcares en la vista de receta, con el mensaje **"Información nutricional no disponible"** cuando no haya datos |
| 4.7 | Links muertos del navbar | `layouts/navbar/navbar.html` | Quitar el link a `/empleados`: esa página es un stub que duplica `pages/admin/usuarios/` y ningún requisito la pide. El de `/ventas` lo reemplaza Dev 1 en 1.9 |
| 4.8 | Estados consistentes | Todas las vistas | Unificar `cargando` / `error` / `vacío`. Hoy cada vista lo maneja a su manera |

---

## Dependencias y orden de ejecución

| Dependencia | Quién depende | Qué necesita |
|:---|:---|:---|
| 3.1 Bootstrap | Devs 1, 2, 4 | Las clases de Bootstrap para maquetar. **Se entrega el día 1** |
| 3.4 `shared/` | Devs 1, 2, 4 | Tabla, modal, botón, toast. Se puede empezar a consumir en el sprint 1 |
| 1.3 / 1.5 N-N | Dev 4 | El DTO de producto que devuelve la respuesta. **Dev 4 empieza su CRUD en el sprint 1, después de 1.5** |
| 1.1 / 1.2 Flyway | Devs 2, 3, 4 | Nadie debe tocar `database/` ni `application.properties` |
| 3.5 Toast | Devs 1, 2, 4 | Para avisar éxito y error en cada acción (RNF-015) |

> **Regla de propiedad de archivos:** cada tabla de la sección anterior indica qué
> archivos puede tocar cada developer. Si necesitas un archivo de otro, avisa; no
> lo edites. Así no hay conflictos al integrar.

---

## Estado de avance

Punto de partida de esta segunda etapa: **todo lo siguiente está pendiente.**

| Developer | Tarea | Estado |
|:---|:---|:---|
| **Dev 1** | 1.1 – 1.5 Migraciones Flyway + relación N-N `producto ↔ proveedor` | Pendiente |
| **Dev 1** | 1.6 – 1.9 Pantalla de Ventas (POS) con Signals | Pendiente |
| **Dev 1** | 1.10 – 1.11 Concurrencia (`@Version`) y formato `PED-{fecha}-{secuencia}` | Pendiente |
| **Dev 1** | 1.12 Columnas nutricionales en `V3` | Pendiente |
| **Dev 2** | 2.1 – 2.5 Reporte Kardex con filtros y export CSV | Pendiente |
| **Dev 2** | 2.6 – 2.7 Pantalla `/admin/kardex` | Pendiente |
| **Dev 3** | 3.1 – 3.3 Bootstrap 5 y diseño responsivo | Pendiente |
| **Dev 3** | 3.4 – 3.6 Componentes `shared/` y servicio de Toast | Pendiente |
| **Dev 3** | 3.7 Migración de vistas a `shared/` | Pendiente |
| **Dev 4** | 4.1 – 4.3 DTOs, CRUD de productos y `PUT` de usuarios | Pendiente |
| **Dev 4** | 4.4 Accesibilidad | Pendiente |
| **Dev 4** | 4.5 – 4.6 Formularios reactivos e info nutricional | Pendiente |
| **Dev 4** | 4.7 – 4.8 Links muertos y estados consistentes | Pendiente |

### Verificación de cierre

| Comprobación | Comando | Resultado esperado |
|:---|:---|:---|
| Backend compila | `mvn clean compile` en `backend/` | Sin errores |
| Frontend compila | `ng build` en `frontend/` | Sin errores ni warnings |
| Base migrada | Arrancar con Flyway activo | `V1`, `V2` y `V3` aplicadas, datos intactos |
| N-N funciona | `SELECT * FROM producto_proveedor` | 12 filas migradas desde `producto.proveedor_id` |
| Kardex filtra | `GET /api/admin/reportes/kardex?tipo=SALIDA` | Solo salidas, con stock anterior y nuevo |
| CSV descarga | `GET /api/admin/reportes/kardex/csv` | Archivo con acentos correctos |
| POS registra | Login → `/ventas` → 2 bebidas → cobrar | Venta `PED-20260928-0001`, stock descontado, kardex con la salida |
| Concurrencia | Dos ventas simultáneas sobre el mismo insumo | La segunda devuelve **409** |

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

No cambian, pero se日系 más código, así que conviene tenerlas presentes:

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
| **Developer 1 (Elvis)** | POS + modelo de datos + concurrencia | 1.1 – 1.12 | **ALTA** | — |
| **Developer 2** | Kardex y exportación | 2.1 – 2.7 | **MEDIA** | — |
| **Developer 3** | Bootstrap, `shared/`, Toast | 3.1 – 3.7 | **MEDIA-BAJA** | — |
| **Developer 4** | CRUD faltante, accesibilidad, formularios | 4.1 – 4.8 | **BAJA** | Dev 1 (1.5) |

> El reparto es **deliberadamente desigual**: Dev 1 carga con el flujo que hace que el
> sistema venda y con el modelado de datos, que es lo más difícil de revertir. Dev 4
> carga con más items, pero todos son mecánicos y de bajo riesgo.

---

## Archivos nuevos en esta etapa

```
backend/
└── src/main/resources/db/migration/          ← NUEVO (Dev 1)
    ├── V1__baseline.sql
    ├── V2__n_n_proveedor.sql
    └── V3__datos_nutricionales.sql

backend/src/main/java/com/munaqfit/backend/
├── model/ProductoProveedor.java              ← NUEVO (Dev 1)
├── repository/ProductoProveedorRepository.java  ← NUEVO (Dev 1)
├── repository/MovimientoInventarioRepository.java  ← Dev 2 (agrega @Query)
├── service/ReporteKardexService.java         ← NUEVO (Dev 2)
├── controller/ReporteKardexController.java   ← NUEVO (Dev 2)
├── dto/KardexFilaDTO.java                    ← NUEVO (Dev 2)
├── dto/ProductoRequestDTO.java               ← NUEVO (Dev 4)
└── dto/ProductoResponseDTO.java              ← NUEVO (Dev 1, compartido con Dev 4)

frontend/src/app/
├── pages/ventas/                             ← Dev 1 (reemplaza el stub)
├── pages/admin/kardex/                       ← NUEVO (Dev 2)
├── service/carrito.service.ts                ← NUEVO (Dev 1)
├── service/toast.service.ts                  ← NUEVO (Dev 3)
├── shared/                                   ← NUEVO (Dev 3)
│   ├── tabla/  modal/  boton/  input/
│   ├── badge/  card/   toast/
│   └── estado-vacio/  spinner/
└── layouts/navbar/navbar.html                ← Devs 1 y 4 (coordinar)
```

---

## Contratos de API que fija Dev 1 y consume Dev 4

Estos nombres no se cambian sin avisar, para que el frontend y el backend no se separen.

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

## Primera Acción para Developer 1 (Elvis)

**Hoy mismo:**

1. **Instala Bootstrap 5** y regístralo en `angular.json` (es lo que desbloquea a los
   otros tres). Si lo haces tú, Dev 3 empieza con la parte 2 de su tarea 3.1.
2. **Crea `V1__baseline.sql`** partiendo de `database/schema.sql`, quitándole el
   `DROP DATABASE` y el `CREATE DATABASE`. Verifica que la base actual se reconstruya
   idéntica antes de tocar nada más.
3. **Crea `V2__n_n_proveedor.sql`**: primero la tabla puente, luego el `INSERT ... SELECT`
   para migrar los proveedores actuales, y solo después el `ALTER TABLE` que elimina
   `producto.proveedor_id`. En ese orden, para que ningún momento queden datos huérfanos.
4. **Avisa a Dev 4** que `ProductoResponseDTO` ya está listo, para que empiece su CRUD.
