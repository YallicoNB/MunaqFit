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
