# Arquitectura de MunaqFit

Este documento cubre el punto **3.4** de la rúbrica: diagramas C4 (contexto,
contenedores y componentes) y la separación de capas del backend.

## 1. C4 - Contexto

```mermaid
C4Context
    title MunaqFit - Contexto del sistema

    Person(admin, "Administrador", "Gestiona inventario, usuarios, reportes y ventas")
    Person(empleado, "Empleado", "Atiende el POS, consulta menú, recetas y órdenes")

    System(frontend, "MunaqFit Web", "Angular 20: login, POS, menú, inventario, reportes")
    System(backend, "MunaqFit API", "Spring Boot 3.5 / Java 17: reglas de negocio y seguridad")
    SystemDb(db, "Base de datos", "MySQL 8 en local / TiDB Cloud en producción")

    System_Ext(izipay, "Izipay", "Pasarela de pagos con tarjeta (sandbox/producción)")
    System_Ext(render, "Render", "Hosting del backend (Docker)")
    System_Ext(vercel, "Vercel", "Hosting del frontend (Angular estático)")

    Rel(admin, frontend, "Usa", "HTTPS")
    Rel(empleado, frontend, "Usa", "HTTPS")
    Rel(frontend, backend, "Consume API REST + SSE", "HTTPS/JSON")
    Rel(backend, db, "Lee y escribe", "JPA/JDBC")
    Rel(backend, izipay, "Solicita token de pago", "HTTPS")
    Rel(render, backend, "Ejecuta el contenedor")
    Rel(vercel, frontend, "Sirve el build estático")
```

## 2. C4 - Contenedores

```mermaid
C4Container
    title MunaqFit - Contenedores

    Person(usuario, "Usuario", "Administrador o empleado")

    Container_Boundary(munaqfit, "MunaqFit") {
        Container(spa, "Frontend Angular", "Angular 20 + Bootstrap 5", "SPA con guards, interceptor JWT, componentes shared y Signals en el POS")
        Container(api, "Backend Spring Boot", "Java 17 + Spring Boot 3.5", "API REST, JWT, SSE, Flyway, Swagger")
        ContainerDb(bd, "Base de datos", "MySQL 8 / TiDB", "Usuarios, bebidas, insumos, ventas, pagos, kardex, recetas")
    }

    System_Ext(izipay, "Izipay", "Checkout embebido / token de sesión")
    System_Ext(render, "Render", "Docker")
    System_Ext(vercel, "Vercel", "CDN estático")

    Rel(usuario, spa, "Interactúa", "HTTPS")
    Rel(spa, api, "REST + EventSource (SSE)", "HTTPS/JSON")
    Rel(api, bd, "JPA + Flyway", "JDBC")
    Rel(api, izipay, "POST /token", "HTTPS")
    Rel(render, api, "Despliega")
    Rel(vercel, spa, "Despliega")
```

## 3. C4 - Componentes del backend

```mermaid
C4Component
    title MunaqFit - Componentes del backend

    Container_Boundary(api, "Backend Spring Boot") {
        Component(security, "Seguridad", "Spring Security", "JwtAuthenticationFilter, RateLimitFilter, SecurityConfig")
        Component(controllers, "Controllers", "Spring MVC", "Auth, Admin*, Empleado*, Producto, Izipay, SSE")
        Component(services, "Services", "Spring Service", "Auth, Venta, Pago, Inventario, Producto, Reporte, Fidelidad, Izipay, SSE")
        Component(repos, "Repositories", "Spring Data JPA", "Usuario, Bebida, Producto, Venta, Pago, Kardex, Receta...")
        Component(dtos, "DTOs", "Java records/classes", "PageResponse, LoginResponse, InventarioProductoDTO, VentaResumenDTO...")
        Component(exception, "Exception Handler", "RestControllerAdvice", "Contrato {timestamp,status,message}")
        Component(swagger, "OpenAPI", "springdoc", "/v3/api-docs y Swagger UI")
    }

    ContainerDb(bd, "Base de datos", "MySQL/TiDB")

    Rel(security, controllers, "Autentica y autoriza")
    Rel(controllers, services, "Delega reglas de negocio")
    Rel(services, repos, "Consulta y persiste")
    Rel(repos, bd, "SQL")
    Rel(controllers, dtos, "Expone")
    Rel(exception, controllers, "Traduce excepciones")
    Rel(swagger, controllers, "Documenta")
```

## 4. Capas y dependencias

| Capa | Paquete | Responsabilidad | Depende de |
|:--|:--|:--|:--|
| Entrada HTTP | `controller` | Validar parámetros, llamar servicios, devolver DTOs | `service`, `dto`, `security` |
| Negocio | `service` | Reglas de venta, pago, inventario, reportes, auth | `repository`, `model`, `dto` |
| Persistencia | `repository` | Consultas Spring Data / JPQL | `model` |
| Modelo | `model` | Entidades JPA | — |
| Contratos | `dto` | Request/Response y `PageResponse` | — |
| Seguridad | `security`, `config` | JWT, rate limit, CORS, Swagger | `service`, `repository` |
| Errores | `exception` | Contrato uniforme de errores | — |

**Flujo típico:** `Controller → Service → Repository → MySQL`.
Los controllers simples de administración (usuarios, proveedores, menú) aún
usan el repositorio directamente; es una deuda técnica menor que se puede
refactorizar sin cambiar la API.

## 5. Decisiones relevantes

- **Paginación estándar:** `PageResponse<T>` con `contenido`, `pagina`,
  `tamano`, `totalElementos`, `totalPaginas`.
- **Ordenamiento:** `?orden=campo,asc|desc` con lista blanca por endpoint.
- **Seguridad:** access token de 15 min + refresh de 7 días rotado con `jti`.
- **Tiempo real:** SSE para alertas de stock, con keep-alive y reconexión.
- **Pagos:** Izipay server-side; en local `IZIPAY_MOCK=true` simula la aprobación.
- **Migraciones:** Flyway (`V1`–`V13`) es la fuente del esquema y del catálogo
  de bebidas/recetas.
