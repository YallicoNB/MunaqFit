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

## Rama principal

La rama por defecto del repositorio es `main`. Los integrantes deben trabajar en ramas por feature (por ejemplo `feat/usuario-crud`, `feat/ventas`) y abrir Pull Requests hacia `main`.
