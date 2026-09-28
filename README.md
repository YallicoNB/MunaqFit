# MunaqFit Manager

Sistema de gestión para **Munaq Fit**, tienda de bebidas saludables (Ubicación: Av. Independencia 198, Ate).

## Estructura del proyecto

| Carpeta | Descripción |
|:---|:---|
| `backend/` | API REST en Spring Boot (Java 17, Maven, JPA, MySQL, Spring Security + JWT) |
| `database/` | Scripts SQL: `schema.sql` (estructura) y `seed.sql` (datos de prueba) |
| `reparto de tareas.md` | Reparto de tareas por developers / plan de sprints |
| `requerimentos.md` | Documento de requerimientos del sistema |

## Requisitos previos

- **JDK 17** (o superior; el proyecto está targeteado a Java 17)
- **Maven 3.6+**
- **MySQL 8.x** corriendo localmente en el puerto `3306`

## Puesta en marcha

### 1. Base de datos

Dentro de la base de datos MySQL **`munaqfit`**, ejecutar en orden:

```sql
source database/schema.sql;
source database/seed.sql;
```

O desde la línea de comandos (ajusta usuario/contraseña):

```sh
mysql -u root -p munaqfit < database/schema.sql
mysql -u root -p munaqfit < database/seed.sql
```

> **Nota de credenciales:** el `application.properties` está configurado con usuario `root` y contraseña `root123`. Si tu contraseña es distinta, cámbiala en `backend/src/main/resources/application.properties`.

### 2. Backend

```sh
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### 3. Usuario de prueba

| DNI | Contraseña | Rol |
|:---|:---|:---|
| `12345678` | `12345678` | ADMIN |

## Endpoint de autenticación

`POST /api/auth/login`

```json
{
  "dni": "12345678",
  "password": "12345678"
}
```

Responde con un token JWT (`Bearer`) que debe enviarse en el header `Authorization` para acceder al resto de la API.

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

## Rama principal

La rama por defecto del repositorio es `main`. Los integrantes deben trabajar en ramas por feature (por ejemplo `feat/usuario-crud`, `feat/ventas`) y abrir Pull Requests hacia `main`.
