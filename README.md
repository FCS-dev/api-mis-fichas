# Mis Fichas APP

API REST para la gestión de micro-finanzas personales. Permite a cada usuario registrar y consultar sus propios
ingresos (`INCOME`) y gastos (`EXPENSE`), organizados por categorías y subcategorías.

## Tecnologías

- **Java** 17
- **Spring Boot** 3.4.6
- **Spring Data JPA** / Hibernate
- **Spring Security** + JWT
- **MariaDB** 11.8
- **Maven** (wrapper incluido: `./mvnw`)
- **Lombok**
- **Docker Compose**

## Requisitos previos

- [Docker](https://docs.docker.com/get-docker/) y Docker Compose
- Java 17
- Copia del archivo `.env` con las variables configuradas:
  ```bash
  cp .env.example .env
  ```

## Variables de entorno

El archivo `.env` debe contener al menos las siguientes variables:

```env
# MariaDB
DB_HOST=localhost
DB_PORT=3306
DB_NAME=misfichasDB
DB_USER=your_db_user
DB_PASSWORD=your_db_password
MARIADB_ROOT_PASSWORD=yout_root_password

# JPA/Hibernate
DDL_AUTO=update
SHOW_SQL=true

# Aplicación
SERVER_PORT=8080

# JWT
JWT_SECRET=your-256-bit-secret-key-here-for-jwt-signing-must-be-at-least-32-characters-long
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=2592000000

# ADMIN
ADMIN_EMAIL=admin@your-domain.com
ADMIN_PASSWORD=your_admin_password
```

## Ejecutar

1. Levantar la base de datos:
   ```bash
   docker compose up -d
   ```

2. Exportar las variables de entorno:
   ```bash
   export $(grep -v '^#' .env | xargs)
   ```

3. Compilar y ejecutar:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Ejecutar tests:
   ```bash
   ./mvnw test
   ```

## Autenticación y seguridad

- **Access Token**: JWT con 15 minutos de expiración, enviado en `Authorization: Bearer <token>`.
- **Refresh Token**: token aleatorio de 30 días, almacenado hasheado (SHA-256) en la tabla `refresh_tokens`. Se entrega
  en una cookie `HttpOnly` llamada `refresh_token` con path `/api/v1/auth`.
- **Rotación de refresh tokens**: cada uso del refresh token revoca el anterior y genera uno nuevo. Si se reutiliza un
  token revocado, se invalidan **todos** los tokens del usuario.
- **Roles**: `USER` (gestiona sus propios datos) y `ADMIN` (gestión global del sistema).
- **Soft delete**: todas las entidades principales usan `deletedAt` en lugar de eliminación física.

## Endpoints de la API

Todos los endpoints están prefijados con `/api/v1`.

### Autenticación (públicos)

| Método | Endpoint                | Descripción                                                           |
|--------|-------------------------|-----------------------------------------------------------------------|
| `POST` | `/api/v1/auth/register` | Registro de nuevo usuario (rol `USER`)                                |
| `POST` | `/api/v1/auth/login`    | Inicio de sesión. Devuelve access token y establece cookie de refresh |
| `POST` | `/api/v1/auth/refresh`  | Obtiene un nuevo access token usando la cookie de refresh             |
| `POST` | `/api/v1/auth/logout`   | Cierra sesión revocando el refresh token                              |

### Categorías (autenticado)

| Método | Endpoint             | Rol         | Descripción                                    |
|--------|----------------------|-------------|------------------------------------------------|
| `GET`  | `/api/v1/categories` | Autenticado | Listar todas las categorías activas (paginado) |

### Subcategorías (autenticado)

| Método   | Endpoint                     | Rol         | Descripción                                         |
|----------|------------------------------|-------------|-----------------------------------------------------|
| `GET`    | `/api/v1/subcategories`      | Autenticado | Listar subcategorías accesibles (sistema + propias) |
| `POST`   | `/api/v1/subcategories`      | Autenticado | Crear subcategoría personal                         |
| `PUT`    | `/api/v1/subcategories/{id}` | Autenticado | Actualizar subcategoría propia                      |
| `DELETE` | `/api/v1/subcategories/{id}` | Autenticado | Eliminar subcategoría propia                        |

### Transacciones (autenticado)

| Método   | Endpoint                    | Rol         | Descripción                                                                 |
|----------|-----------------------------|-------------|-----------------------------------------------------------------------------|
| `GET`    | `/api/v1/transactions`      | Autenticado | Listar transacciones (paginado, filtros por fecha, categoría, subcategoría) |
| `POST`   | `/api/v1/transactions`      | `USER`      | Crear transacción propia                                                    |
| `PUT`    | `/api/v1/transactions/{id}` | Autenticado | Actualizar transacción (USER: solo propias; ADMIN: solo de usuarios USER)   |
| `DELETE` | `/api/v1/transactions/{id}` | Autenticado | Eliminar transacción (USER: solo propias; ADMIN: solo de usuarios USER)     |

### Administración (rol `ADMIN`)

| Método   | Endpoint                        | Descripción                                                            |
|----------|---------------------------------|------------------------------------------------------------------------|
| `GET`    | `/api/v1/admin/users`           | Listar usuarios (paginado, filtros por `role` y `status`)              |
| `GET`    | `/api/v1/admin/users/{id}`      | Ver un usuario por ID                                                  |
| `PUT`    | `/api/v1/admin/users/{id}`      | Actualizar usuario (nombre, email, rol, estado — incluyendo `BLOCKED`) |
| `DELETE` | `/api/v1/admin/users/{id}`      | Soft delete de usuario                                                 |
| `GET`    | `/api/v1/admin/categories`      | Listar todas las categorías (paginado)                                 |
| `POST`   | `/api/v1/admin/categories`      | Crear categoría                                                        |
| `PUT`    | `/api/v1/admin/categories/{id}` | Actualizar categoría                                                   |
| `DELETE` | `/api/v1/admin/categories/{id}` | Soft delete de categoría                                               |

### Paginación

Los endpoints de listado soportan los siguientes parámetros opcionales:

| Parámetro | Default              | Descripción                                                       |
|-----------|----------------------|-------------------------------------------------------------------|
| `page`    | `0`                  | Número de página (0-based)                                        |
| `size`    | `20`                 | Tamaño de página (máximo 100)                                     |
| `sort`    | depende del endpoint | Criterio de ordenamiento, ej: `name,asc` o `transactionDate,desc` |

### Filtros de transacciones

| Parámetro       | Descripción                                                                               |
|-----------------|-------------------------------------------------------------------------------------------|
| `userId`        | Filtrar por usuario (solo `ADMIN`; los `USER` siempre ven solo sus propias transacciones) |
| `categoryId`    | Filtrar por categoría                                                                     |
| `subcategoryId` | Filtrar por subcategoría                                                                  |
| `date`          | Fecha exacta (`YYYY-MM-DD`)                                                               |
| `dateFrom`      | Inicio de rango de fechas                                                                 |
| `dateTo`        | Fin de rango de fechas                                                                    |

## Estructura del proyecto

```
com.fcs.mis_fichas
├── config
│   ├── AdminSeeder.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
├── controllers
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── CategoryPublicController.java
│   ├── SubcategoryController.java
│   ├── TransactionController.java
│   ├── UserController.java
│   └── GlobalExceptionHandler.java
├── dtos
│   ├── ApiResponse.java
│   ├── AuthResponse.java
│   ├── CategoryRequest.java / CategoryResponse.java
│   ├── LoginRequest.java / RegisterRequest.java
│   ├── PagedResponse.java / PaginationInfo.java
│   ├── SubcategoryRequest.java / SubcategoryResponse.java
│   ├── TransactionRequest.java / TransactionResponse.java
│   ├── UserResponse.java / UserUpdateRequest.java
│   └── ...
├── entities
│   ├── Category.java
│   ├── RefreshToken.java
│   ├── Subcategory.java
│   ├── Transaction.java
│   └── User.java
├── enums
│   ├── Role.java (USER, ADMIN)
│   ├── Status.java (ACTIVE, BLOCKED)
│   └── Type.java (INCOME, EXPENSE)
├── repositories
│   ├── CategoryRepository.java
│   ├── RefreshTokenRepository.java
│   ├── SubcategoryRepository.java
│   ├── TransactionRepository.java
│   └── UserRepository.java
├── services
│   ├── AuthService.java
│   ├── CategoryService.java
│   ├── JwtService.java
│   ├── RefreshTokenService.java
│   ├── SubcategoryService.java
│   ├── TransactionService.java
│   ├── UserDetailsServiceImpl.java
│   └── UserService.java
└── MisFichasApplication.java
```

## Notas adicionales

- **Admin seeding**: al arrancar la aplicación se crea automáticamente un usuario `ADMIN` si no existe, usando las
  credenciales de `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
- **Categorías y subcategorías de sistema**: se insertan automáticamente al primer arranque (`AdminSeeder`). Son de solo
  lectura para los usuarios `USER`.
- **Schema**: Hibernate `ddl-auto` está configurado como `update` (configurable vía `DDL_AUTO` en `.env`). Esto muta el
  schema automáticamente en cada arranque. Se recomienda usar `validate` en producción y migraciones con Flyway o
  Liquibase en el futuro.
- **Tests**: actualmente solo existe un test de integración (`@SpringBootTest`) que verifica que el contexto de Spring
  carga correctamente. Los tests requieren una base de datos MariaDB activa.
- **Problemas de compilación**: si aparece `ConflictingBeanDefinitionException` por controladores duplicados, ejecutar
  `./mvnw clean` antes de compilar.
