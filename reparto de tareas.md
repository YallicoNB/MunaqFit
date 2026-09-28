# Reparto de Tareas - MunaqFit Manager

---

## ¿Por qué empezar por el Backend?

| Razón | Explicación |
|:---|:---|
| **1. Base sólida** | El backend es el corazón del sistema. Si la API funciona, el frontend puede consumirla sin problemas |
| **2. Definición de contratos** | Los endpoints definen qué datos se envían y reciben. El frontend se basa en esto |
| **3. Desarrollo paralelo** | Una vez que el backend tiene endpoints básicos, el frontend puede empezar a consumirlos |
| **4. Pruebas tempranas** | Puedes probar la API con Postman/Insomnia antes de construir el frontend |
| **5. Menos bloqueos** | El frontend no se bloquea esperando datos reales |

---

## Estrategia de Desarrollo por Sprints

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     PLAN DE DESARROLLO (SPRINTS)                          │
├─────────────────────────────────────────────────────────────────────────────┤
```

---

## División de Tareas por Developers (4 Integrantes)

---

### Developer 1 (Noe) — Backend Base + Arquitectura

**Objetivo:** Crear la base del backend para que los demás puedan trabajar.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 1.1 | Configurar proyecto Spring Boot | `pom.xml`, `application.properties` | Dependencias: Spring Web, JPA, MySQL, Security, JWT, Lombok |
| 1.2 | Crear entidades base | `model/Usuario.java`, `model/Bebida.java`, `model/Venta.java`, `model/DetalleVenta.java`, `model/Producto.java`, `model/Proveedor.java`, `model/Categoria.java`, `model/Pago.java`, `model/MovimientoInventario.java`, `model/ClienteFidelidad.java`, `model/VisitaCliente.java` | Todas las entidades del modelo de datos |
| 1.3 | Crear repositorios JPA | `repository/*Repository.java` | Interfaces para CRUD de cada entidad |
| 1.4 | Configurar seguridad | `config/SecurityConfig.java`, `config/JwtConfig.java`, `security/JwtAuthenticationFilter.java`, `security/JwtTokenProvider.java`, `security/CustomUserDetailsService.java` | Spring Security + JWT |
| 1.5 | Implementar AuthController | `controller/AuthController.java` | Login, recuperación de contraseña, logout |
| 1.6 | Crear DTOs base | `dto/LoginRequest.java`, `dto/LoginResponse.java`, `dto/UsuarioDTO.java` | Data Transfer Objects |
| 1.7 | Scripts de base de datos | `database/schema.sql`, `database/seed.sql` | Creación de tablas y datos de prueba |

---

### Developer 2 — Backend Admin (CRUD + Reportes)

**Objetivo:** Implementar todos los controladores del panel de administración.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 2.1 | AdminDashboardController | `controller/AdminDashboardController.java` | Estadísticas: ventas hoy, total clientes, productos activos, alertas de stock |
| 2.2 | AdminUsuarioController | `controller/AdminUsuarioController.java` | CRUD de empleados (crear, listar, eliminar) |
| 2.3 | AdminInventarioController | `controller/AdminInventarioController.java` | Ver stock, agregar stock, alertas de stock crítico |
| 2.4 | AdminProveedorController | `controller/AdminProveedorController.java` | CRUD de proveedores |
| 2.5 | AdminReportesController | `controller/AdminReportesController.java` | Reportes: ranking de bebidas, ventas por período, valorización |
| 2.6 | AdminVentaController | `controller/AdminVentaController.java` | Ver todas las ventas, cambiar estado de ventas |
| 2.7 | Servicios de negocio | `service/InventarioService.java`, `service/ReporteService.java` | Lógica de negocio para inventario y reportes |

---

### Developer 3 — Backend Empleado (Ventas + Órdenes)

**Objetivo:** Implementar los controladores para empleados.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 3.1 | EmpleadoVentaController | `controller/EmpleadoVentaController.java` | Tomar pedidos, ver menú, registrar pagos |
| 3.2 | EmpleadoOrdenController | `controller/EmpleadoOrdenController.java` | Ver órdenes del día, detalle de órdenes |
| 3.3 | EmpleadoMenuController | `controller/EmpleadoMenuController.java` | Ver catálogo de bebidas, consultar recetas |
| 3.4 | EmpleadoFidelidadController | `controller/EmpleadoFidelidadController.java` | Registrar clientes frecuentes, registrar visitas, aplicar décima visita |
| 3.5 | Servicios de negocio | `service/VentaService.java`, `service/FidelidadService.java` | Lógica para ventas (descuento de stock, cálculo de totales) |
| 3.6 | Procesamiento de pagos | `service/PagoService.java` | Registro de pagos, cálculo de vuelto |

---

### Developer 4 — Frontend (Angular)

**Objetivo:** Implementar toda la interfaz de usuario.

| # | Task | Archivos | Descripción |
|:---|:---|:---|:---|
| 4.1 | Configurar proyecto Angular | `angular.json`, `package.json` | Dependencias, Bootstrap, Font Awesome |
| 4.2 | Crear servicios API | `services/auth.service.ts`, `services/venta.service.ts`, `services/producto.service.ts`, `services/inventario.service.ts`, `services/usuario.service.ts` | Consumir endpoints del backend |
| 4.3 | Interceptor y Guards | `interceptors/auth.interceptor.ts`, `guards/auth.guard.ts`, `guards/role.guard.ts` | Manejo de JWT y protección de rutas |
| 4.4 | Módulo de Autenticación | `auth/login/login.component.ts`, `auth/login/login.component.html` | Pantalla de login |
| 4.5 | Layout y Dashboard | `shared/navbar/navbar.component.ts`, `shared/sidebar/sidebar.component.ts`, `shared/footer/footer.component.ts`, `dashboard/dashboard.component.ts` | Layout base y dashboard |
| 4.6 | Módulo Empleado | `empleado/ventas/ventas.component.ts`, `empleado/ordenes/ordenes.component.ts`, `empleado/menu/menu.component.ts` | Vistas de empleado |
| 4.7 | Módulo Admin | `admin/inventario/inventario.component.ts`, `admin/usuarios/usuarios.component.ts`, `admin/reportes/reportes.component.ts` | Vistas de administrador |
| 4.8 | Integración y pruebas | Todos los archivos | Conectar todas las vistas con el backend |

---

## Estado de avance

Actualizado tras integrar las ramas de los 4 developers en `main`.

| Developer | Tarea | Estado |
|:---|:---|:---|
| **Dev 1** | 1.1 – 1.7 Backend base | Completado |
| **Dev 1** | Extra: `GET /api/auth/validar-token` | Completado |
| **Dev 1** | Extra: API de productos (`/api/productos/**`) | Completado |
| **Dev 2** | 2.1 – 2.3, 2.5 – 2.7 Controladores admin y servicios | Completado |
| **Dev 2** | 2.4 `DELETE` de proveedores (faltaba en el CRUD) | Completado |
| **Dev 2** | Arreglo: `porcentajeTotal` del ranking venía `null` | Completado |
| **Dev 2** | Arreglo: `ingresosPorMetodoPago` venía siempre vacío | Completado |
| **Dev 3** | 3.1 – 3.4, 3.4 fidelidad Controladores de empleado | Completado |
| **Dev 3** | 3.5 `VentaService`: faltaba descontar stock y guardar el kardex | Completado |
| **Dev 3** | 3.6 `PagoService`: faltaba guardar el pago y marcar la venta `PAGADO` | Completado |
| **Dev 3** | Arreglo: el detalle de orden devolvía un texto en vez de los datos | Completado |
| **Dev 3** | Arreglo: la receta se buscaba por `id` en vez de por `bebidaId` | Completado |
| **Dev 4** | 4.1 Configuración Angular (proyecto, rutas, guards) | Ya existía |
| **Dev 4** | 4.2 Servicios API (producto, venta, admin, reporte, auth) | Completado |
| **Dev 4** | 4.3 `auth.interceptor.ts` y `role.guard.ts` | Completado |
| **Dev 4** | 4.4 Login | Ya existía |
| **Dev 4** | 4.5 Navbar, footer, sidebar y dashboard | Completado |
| **Dev 4** | 4.6 Módulo empleado: menú y órdenes | Completado |
| **Dev 4** | 4.7 Módulo admin: inventario, usuarios y reportes | Completado |
| **Dev 4** | 4.8 Integración con la API | Completado |
| — | Arreglo: datos de `seed.sql` no permitían vender 2 bebidas | Completado |
| **Dev 1** | Arreglo: la recuperación de contraseña devolvía 200 **sin cambiar la clave** | Completado |
| **Dev 1** | Arreglo: la respuesta revelaba qué correos estaban registrados | Completado |
| — | `Mi cuenta`: perfil del usuario con los datos de la sesión | Completado |
| — | Eliminada la página `/register` y su `permitAll` (el backend nunca tuvo ese endpoint) | Completado |

### Auditoría y normalización de la base de datos

Revisión completa de `schema.sql` contra práctica real (3FN, claves foráneas,
restricciones, tipos de fecha). 12 problemas encontrados y corregidos. Ver la
sección **"Modelo de datos"** del `README.md` para el detalle.

| Cambio | Por qué |
|:---|:---|
| `categoria_bebida` (nueva) + `bebida.categoria_id` como FK | `bebida.categoria` era texto suelto con 8 valores que no coincidían con ninguno de los 6 de `categoria`. Cero integridad. |
| Se eliminó `receta.unidad` | La cantidad va en la unidad canónica de `producto.unidad_medida`. Borró ~115 líneas de conversión G↔KG / ML↔L. |
| Se eliminó `pago.monto_total` | Dependencia transitiva y violación de 3FN. Ahora se lee de `venta.total`. |
| Se eliminó `producto.precio_venta` | Columna muerta: el insumo no se vende solo. |
| Tabla `parametro` (nueva) con la tasa de IGV | Estaba fija como constante en `VentaService`. Ahora es configurable sin recompilar. |
| `venta.igv_tasa` (nueva) | Instantánea: si la tasa sube, los reportes viejos siguen siendo correctos. |
| 33 restricciones `CHECK` (antes 0) | `stock_actual >= 0`, `stock_critico <= stock_minimo`, `receta.cantidad > 0`, `total = subtotal + igv`, `monto_pagado > 0`… |
| 10 restricciones `UNIQUE` (antes 2) | Un pago por venta, una visita por venta, un insumo por bebida, DNI de cliente único, entre otras. |
| `cliente_fidelidad.dni` (nuevo, `UNIQUE`) | Sin DNI el mismo cliente podía registrarse Infinity veces. |
| `venta.numero_pedido` pasó a `NOT NULL UNIQUE` | Un ticket sin número no es un ticket, y el número se repitía si se borraba una venta. |
| `movimiento_inventario.tipo_referencia` (nuevo) | `referencia_id` es polimórfica y no puede tener FK; el tipo le da sentido. |
| 10 `TIMESTAMP` → `DATETIME` | `TIMESTAMP` acaba en 2038 y se desplaza solo si cambia la zona horaria del servidor. |
| `Categoria`: quitar `CascadeType.ALL` | Borrar una categoría arrastraba el stock de todos sus productos. |
| `PagoService`: el vuelto solo en efectivo | Con Yape/Plin/QR no hay billetes que devolver; el monto debe ser exacto. |
| 404/400/405/401 en vez de 500 | Errores del cliente se reportaban como caída del servidor, y el 500 filtraba detalles internos. |
| `numero_pedido` usa `MAX(id)+1` en vez de `count()+1` | `count()` baja si se borra una venta y el número se repite. |

### Pendiente: pantallas de relleno de Dev 4

Estas dos páginas se generaron con `ng generate` y nunca se Desarrollaron. Su HTML es
literalmente `<p>ventas works!</p>` y `<p>empleados works!</p>`, y ambas tienen su `.css`
en 0 bytes. Se conservaron tal cual, sin desarrollar.

| Página | Ruta | Contexto |
|:---|:---|:---|
| `pages/ventas/` | `/ventas` | Duplica el flujo de venta de `pages/ordenes/`, que ya lo cubre completo. No está en el sidebar. |
| `pages/empleados/` | `/empleados` | Duplica la gestión de empleados de `pages/admin/usuarios/`. No está en el sidebar. |

⚠️ **Los dos siguen enlazados desde el navbar de `pages/home`** (`navbar.html`), que solo los
muestra cuando el rol es `ADMIN`. Para cerrarlo del todo hay que quitar esas dos líneas
del navbar o rellenar las páginas.

### Reglas de venta

- El empleado que registra la venta se toma del **token JWT**, no del cuerpo del request.
- El IGV es **18%**.
- Cada venta descuenta el stock de los insumos **según la receta** de la bebida y
  deja un movimiento `SALIDA` en el kardex. La cantidad de la receta va siempre en
  la **unidad canónica** del insumo (`producto.unidad_medida`), así que **no hace
  falta convertir entre `G`/`KG` ni `ML`/`L`**: la columna `receta.unidad` ya no
  existe. Ver la sección «Auditoría y normalización de la base de datos».
- Si un insumo queda sin stock la venta se rechaza y no se guarda nada.
- No se puede pagar dos veces la misma venta, ni con monto menor al total.

### Reglas de recuperación de contraseña

- La respuesta de `POST /api/auth/forgot-password` es **siempre la misma**, exista o no el
  correo. Antes devolvía `400` cuando el correo no estaba registrado, lo que permitía
  enumerar qué correos hay en el sistema.
- El token de recuperación es de **un solo uso** y expira a los **60 minutos**. Se guarda
  en memoria (`ConcurrentHashMap`) porque el proyecto no incluye envío de correo.
- `POST /api/auth/reset-password` **sí cambia la contraseña** (antes validaba los datos
  y devolvía `200` sin hacer nada). También limpia el bloqueo por intentos fallidos.

---

## Resumen de Tareas por Developer

| Developer | Responsabilidad | Tareas | Prioridad |
|:---|:---|:---|:---|
| **Developer 1 (Noe)** | Backend Base + Arquitectura | Entidades, Repositorios, Seguridad, Auth, DTOs, SQL | **ALTA** (Base para todos) |
| **Developer 2** | Backend Admin | Dashboard, Usuarios, Inventario, Proveedores, Reportes | **MEDIA** (Depende de Dev 1) |
| **Developer 3** | Backend Empleado | Ventas, Órdenes, Menú, Fidelidad, Pagos | **MEDIA** (Depende de Dev 1) |
| **Developer 4** | Frontend Angular | Servicios, Guards, Layout, Vistas, Integración | **MEDIA** (Depende de Devs 1-3) |

---

## Estructura del Repositorio Git

```
munaqfit-manager/
├── backend/
│   ├── src/main/java/com/munaqfit/backend/
│   │   ├── MunaqfitBackendApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtConfig.java
│   │   │   └── CorsConfig.java
│   │   ├── security/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── CustomUserDetailsService.java
│   │   ├── model/
│   │   │   ├── Usuario.java
│   │   │   ├── Bebida.java
│   │   │   ├── Venta.java
│   │   │   ├── DetalleVenta.java
│   │   │   ├── Producto.java
│   │   │   ├── Proveedor.java
│   │   │   ├── Categoria.java
│   │   │   ├── Pago.java
│   │   │   ├── MovimientoInventario.java
│   │   │   ├── ClienteFidelidad.java
│   │   │   └── VisitaCliente.java
│   │   ├── repository/
│   │   │   ├── UsuarioRepository.java
│   │   │   ├── BebidaRepository.java
│   │   │   └── ... (todos los repositorios)
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── VentaService.java
│   │   │   └── ... (todos los servicios)
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── Admin/
│   │   │   │   ├── AdminDashboardController.java
│   │   │   │   ├── AdminUsuarioController.java
│   │   │   │   ├── AdminInventarioController.java
│   │   │   │   └── AdminReportesController.java
│   │   │   └── Empleado/
│   │   │       ├── EmpleadoVentaController.java
│   │   │       ├── EmpleadoOrdenController.java
│   │   │       └── EmpleadoMenuController.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   └── ... (todos los DTOs)
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       └── CustomExceptions.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── frontend/
│   ├── src/app/
│   │   ├── core/
│   │   │   ├── guards/
│   │   │   ├── interceptors/
│   │   │   └── services/
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   └── models/
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   ├── admin/
│   │   │   └── empleado/
│   │   ├── app-routing.module.ts
│   │   └── app.module.ts
│   └── package.json
│
└── database/
    ├── schema.sql
    └── seed.sql
```

---

## Primera Acción para Developer 1 (Noe)

**Hoy mismo haz esto:**

1. **Crear el repositorio en GitHub** y compartirlo con tu equipo.
2. **Crear el proyecto Spring Boot** desde [Spring Initializr](https://start.spring.io/).
3. **Crear el script SQL** de la base de datos (ya lo tenemos).
4. **Subir el código base** a GitHub.
5. **Comunicar a tu equipo** que ya pueden clonar el proyecto.
