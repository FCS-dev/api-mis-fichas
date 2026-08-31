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
SPRING_PROFILES_ACTIVE=developer

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

## Perfiles de Spring

El proyecto soporta perfiles de Spring para configurar el comportamiento según el ambiente.

### Perfil DEVELOPER

Al iniciar la app con el perfil `developer`, el `AdminSeeder` carga automáticamente **datos de prueba**:

- **43 usuarios** fake (nombres en español, enero-agosto 2026)
- **~2000 transacciones** con montos realistas y descripciones en español
- Password de cada usuario: su propio email (ej: `carlos.garcia@prueba.fcs`)

**Condición**: solo se insertan datos fake si la BD no tiene usuarios además del admin.

Para activar el perfil, asegurate de que `.env` contenga:
```env
SPRING_PROFILES_ACTIVE=developer
```

La config del perfil DEVELOPER (`application-developer.yaml`) incluye:
- `show-sql: true` — queries SQL visibles en consola
- Logging DEBUG para `com.fcs.mis_fichas` y queries Hibernate

### Generar datos fake manualmente (SQL)

Si preferís ejecutar el SQL directamente contra la BD en vez de usar el AdminSeeder:

```bash
cd src/main/resources
pip install bcrypt
python3 generate_fake_data.py
mysql -u admin -p misfichasDB < data-fake.sql
```

El script Python genera `data-fake.sql` con la misma data que el AdminSeeder.

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

| Método | Endpoint                   | Rol         | Descripción                                    |
|--------|----------------------------|-------------|------------------------------------------------|
| `GET`  | `/api/v1/categories`       | Autenticado | Listar todas las categorías activas (paginado) |
| `GET`  | `/api/v1/categories/{id}`  | Autenticado | Obtener una categoría por ID                   |

### Subcategorías (autenticado)

| Método   | Endpoint                                | Rol         | Descripción                                                         |
|----------|-----------------------------------------|-------------|---------------------------------------------------------------------|
| `GET`    | `/api/v1/subcategories`                 | Autenticado | Listar subcategorías accesibles (sistema + propias)                 |
| `GET`    | `/api/v1/subcategories/{id}`            | Autenticado | Obtener una subcategoría por ID                                     |
| `GET`    | `/api/v1/subcategories/category/{categoryId}` | Autenticado | Listar subcategorías de una categoría específica              |
| `POST`   | `/api/v1/subcategories`                 | Autenticado | Crear subcategoría personal                                         |
| `PUT`    | `/api/v1/subcategories/{id}`            | Autenticado | Actualizar subcategoría (USER: propias; ADMIN: cualquier)           |
| `DELETE` | `/api/v1/subcategories/{id}`            | Autenticado | Eliminar subcategoría (USER: propias; ADMIN: cualquier)             |

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

### Dashboard (rol `USER`)

| Método | Endpoint                           | Parámetros                    | Descripción                                                         |
|--------|------------------------------------|-------------------------------|---------------------------------------------------------------------|
| `GET`  | `/api/v1/dashboard/me/total-income`| `month`, `year`               | Sumatoria de ingresos del mes para el usuario autenticado           |
| `GET`  | `/api/v1/dashboard/me/total-expense`| `month`, `year`              | Sumatoria de gastos del mes para el usuario autenticado             |
| `GET`  | `/api/v1/dashboard/me/expenses-by-category`| `month`, `year`       | Gastos agrupados por categoría en un mes/año                        |
| `GET`  | `/api/v1/dashboard/me/expenses-by-subcategory`| `categoryId`, `month`, `year`| Gastos agrupados por subcategoría dentro de una categoría   |
| `GET`  | `/api/v1/dashboard/me/monthly-balance`| _(ninguno)_                | Balance mensual (INCOME - EXPENSE) de los últimos 12 meses          |

### Dashboard (rol `ADMIN`)

| Método | Endpoint                                                  | Parámetros                                                      | Descripción                                                                                     |
|--------|-----------------------------------------------------------|-----------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `GET`  | `/api/v1/dashboard/admin/stats`                           | _(ninguno)_                                                     | Total de usuarios activos con rol USER y total de transacciones registradas                     |
| `GET`  | `/api/v1/dashboard/admin/expenses-by-category`            | `userId`, `monthFrom`, `yearFrom`, `monthTo`, `yearTo`          | Gastos agrupados por categoría en un rango de fechas                                            |
| `GET`  | `/api/v1/dashboard/admin/expenses-by-subcategory`         | `userId`, `categoryId`, `monthFrom`, `yearFrom`, `monthTo`, `yearTo`| Gastos agrupados por subcategoría dentro de una categoría en un rango                      |
| `GET`  | `/api/v1/dashboard/admin/avg-income`                      | `userId`                                                        | Promedio mensual de ingresos (últimos 12 meses)                                                 |
| `GET`  | `/api/v1/dashboard/admin/avg-expense`                     | `userId`                                                        | Promedio mensual de gastos (últimos 12 meses)                                                   |
| `GET`  | `/api/v1/dashboard/admin/user-evolution`                  | `monthFrom`, `yearFrom`, `monthTo`, `yearTo`                    | Evolución de usuarios: comparación 1er vs último mes del rango, datos mensuales                 |
| `GET`  | `/api/v1/dashboard/admin/transaction-evolution`           | `monthFrom`, `yearFrom`, `monthTo`, `yearTo`, `userId`          | Evolución de transacciones: comparativa, promedio/usuario, ingresos/gastos, datos mensuales     |
| `GET`  | `/api/v1/dashboard/admin/money-movement`                  | `userId`                                                        | Totales de ingresos, gastos y balance                                                           |
| `GET`  | `/api/v1/dashboard/admin/averages`                        | `userId`                                                        | Promedios globales (excluye ADMIN) y filtrados por usuario (ingresos, gastos, transacciones)   |
| `GET`  | `/api/v1/dashboard/admin/top-users`                       | `monthFrom`, `yearFrom`, `monthTo`, `yearTo`                    | Top 5 usuarios por transacciones, gastos e ingresos en un rango de meses                       |
| `GET`  | `/api/v1/dashboard/admin/activity-distribution`           | `month`, `year`                                                 | Distribución de usuarios: Frecuente (>20 tx), Regular (5-20), Ocasional (1-4), Inactivo (0)    |

> **Nota sobre `userId` en endpoints ADMIN**: es opcional (default `0`). Si no se envía o es `0`, se incluyen todos los usuarios. Si se envía un valor mayor a `0`, se filtra por ese usuario específico.

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
| `from`          | Fecha inicio del rango (endpoint `/date-range`, opcional — si se omite retorna todas)     |
| `to`            | Fecha fin del rango (endpoint `/date-range`, opcional — si se omite retorna todas)        |

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
│   ├── DashboardController.java
│   ├── SubcategoryController.java
│   ├── TransactionController.java
│   ├── UserController.java
│   └── GlobalExceptionHandler.java
├── dtos
│   ├── ActivityCategory.java
│   ├── ActivityDistributionResponse.java
│   ├── AdminStatsResponse.java
│   ├── ApiResponse.java
│   ├── AuthResponse.java
│   ├── CategoryRequest.java / CategoryResponse.java
│   ├── CategorySummaryResponse.java
│   ├── DashboardAveragesResponse.java
│   ├── DashboardTotalResponse.java
│   ├── LoginRequest.java / RegisterRequest.java
│   ├── MoneyMovementResponse.java
│   ├── MonthlyAverageResponse.java
│   ├── MonthlyBalanceResponse.java
│   ├── PagedResponse.java / PaginationInfo.java
│   ├── PeriodComparison.java / PeriodComparisonDouble.java
│   ├── SubcategoryRequest.java / SubcategoryResponse.java
│   ├── SubcategorySummaryResponse.java
│   ├── TopUserEntry.java / TopUsersResponse.java
│   ├── TransactionEvolutionResponse.java / TransactionEvolutionSummary.java
│   ├── TransactionMonthlyData.java
│   ├── TransactionRequest.java / TransactionResponse.java
│   ├── UserEvolutionResponse.java / UserEvolutionSummary.java
│   ├── UserMonthlyData.java
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
│   ├── DashboardService.java
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
- **Fake data (DEVELOPER)**: si el perfil activo es `developer` y la BD está vacía (solo el admin), el `AdminSeeder`
  crea automáticamente 43 usuarios de prueba y ~2000 transacciones con montos y fechas realistas. Esto permite
  probar la aplicación con datos inmediatamente al iniciar.
- **Categorías y subcategorías de sistema**: se insertan automáticamente al primer arranque (`AdminSeeder`). Son de solo
  lectura para los usuarios `USER`.
- **Schema**: Hibernate `ddl-auto` está configurado como `update` (configurable vía `DDL_AUTO` en `.env`). Esto muta el
  schema automáticamente en cada arranque. Se recomienda usar `validate` en producción y migraciones con Flyway o
  Liquibase en el futuro.
- **Dashboard ADMIN - Evolución de usuarios**: compara el 1er mes del rango seleccionado vs el último mes (no el
  período anterior). Esto permite ver la tendencia de crecimiento a lo largo del tiempo.
- **Dashboard ADMIN - Distribución de actividad**: "Inactivo" cuenta solo usuarios que fueron registrados en el mes
  evaluado y no tienen transacciones (no incluye usuarios registrados en meses anteriores). Los porcentajes se
  calculan sobre el total de usuarios evaluados en ese mes.
- **Dashboard ADMIN - Promedios**: los promedios globales excluyen usuarios con rol `ADMIN`. Los promedios filtrados
  por `userId` dividen la suma total por la cantidad de meses desde el registro del usuario hasta el mes actual.
- **Dashboard ADMIN - Distribución de dinero**: los totales se calculan desde el inicio del sistema (sin filtro de
  fechas), permitiendo ver el acumulado histórico de ingresos, gastos y balance.
- **Transactions - Rango de fechas opcional**: el endpoint `/date-range` acepta `from` y `to` opcionales. Si se
  omiten, retorna todas las transacciones. Esto es utilizado por el panel de transacciones del admin CRUD
  para mostrar todas las transacciones del usuario seleccionado sin filtro temporal.
- **Tests**: existen tests unitarios para servicios y controladores (con `Mockito` y `MockMvc`) y un test de integración
  (`@SpringBootTest`) que verifica que el contexto de Spring carga correctamente.
  Los tests de integración requieren una base de datos MariaDB activa.
- **Problemas de compilación**: si aparece `ConflictingBeanDefinitionException` por controladores duplicados, ejecutar
  `./mvnw clean` antes de compilar.
