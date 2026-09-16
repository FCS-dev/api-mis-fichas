# AGENTS.md — mis-fichas

## Build & Run

- **Build tool**: Maven (`./mvnw`)
- **Java**: 17
- **Spring Boot**: 3.4.6 (per `pom.xml`)

## Database Dependency

- **MariaDB 11.8** is required for both the application and tests.
- **No embedded/H2 database** is configured. Tests fail without a live DB.
- Start the DB before running anything:
  ```bash
  docker compose up -d
  ```
- The container uses credentials from `.env` (mapped to `docker-compose.yml`).

## Environment Variables

- Spring Boot reads config from **environment variables** loaded via `application.yaml`.
- The `.env` file at the repo root is **not automatically loaded** by Spring Boot.
- **Required secrets** (no defaults in `application.yaml`): `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
- Export variables before running Maven:
  ```bash
  export $(grep -v '^#' .env | xargs)
  ```
- Then run:
  ```bash
  ./mvnw spring-boot:run
  ./mvnw test
  ```
- Note: `.env` is listed in `.gitignore` and has never been tracked in the repo.

## Package Structure

- **Root package**: `com.fcs.mis_fichas` (underscores, not hyphens).
- The original `com.fcs.mis-fichas` was invalid per Maven/Spring conventions.
- Main entrypoint: `com.fcs.mis_fichas.MisFichasApplication`

## Testing

- Only integration-level `@SpringBootTest` exists (`MisFichasApplicationTests.contextLoads`).
- Tests spin up the full Spring context and require a reachable MariaDB.
- If you see `ConflictingBeanDefinitionException` for duplicate controllers, run `./mvnw clean` first — stale `.class` files in `target/` from deleted packages can survive source deletions.

## Code Conventions

- **Lombok**: Required for compilation. Annotation processors are configured in `pom.xml` and `.idea/compiler.xml`.
- **Soft delete**: Entities include `private LocalDateTime deletedAt;`. Do not use physical deletion for user-facing records.
- **Entity builders**: Lombok `@Builder` is used on entities. `@AllArgsConstructor` is required alongside it.
- **Enums**: Stored as `STRING` in the database (`@Enumerated(EnumType.STRING)`).

## Application Context

- **Purpose**: REST API so each user can track their own expenses (`EXPENSE`) and incomes (`INCOME`) in the `transactions` table.
- **Categories** (`categories`): plain grouping entities with `name` and `type` (INCOME/EXPENSE). No `isSystem` flag.
- **Subcategories** (`subcategories`): hold the `isSystem` boolean. 
  - *System subcategories* (`isSystem = true`): seeded automatically on startup by `AdminSeeder`. Only an `ADMIN` can modify them.
  - *User subcategories*: `USER` role accounts can create and manage their own personal subcategories under existing categories (basic CRUD).
- **Soft delete**: All main entities use `deletedAt` instead of physical deletion.

## Architecture Notes

- **Controllers**: `com.fcs.mis_fichas.controllers.*`
- **Entities**: `com.fcs.mis_fichas.entities.*`
- **Repositories**: `com.fcs.mis_fichas.repositories.*` (User, Category, Subcategory, RefreshToken)
- **Services**: `com.fcs.mis_fichas.services.*` (JwtService, RefreshTokenService, AuthService, UserDetailsServiceImpl)
- **Enums**: `com.fcs.mis_fichas.enums.*` (Role, Status, Type)

## Team Conventions

- No formal branching, commit message, or code style conventions (e.g. Spotless, Checkstyle) are currently defined.

## Database Migrations

- No decision yet on Flyway or Liquibase. Hibernate `ddl-auto: update` is the current approach and will remain for the near term.

## Authentication & Security

- `spring-boot-starter-security` is now in `pom.xml` (added to support `BCryptPasswordEncoder` and admin seeding).
- **Admin seeder**: `AdminSeeder` runs on startup and creates the default admin account if it does not exist:
  - Credentials are read from `ADMIN_EMAIL` and `ADMIN_PASSWORD` environment variables.
  - Role: `ADMIN`
- **JWT implemented**: `jjwt` library (0.12.3) is in `pom.xml`.
  - **Access Token**: JWT with 15-minute expiration, delivered in `Authorization: Bearer <token>` header.
  - **Refresh Token**: 30-day expiration, cryptographically secure random token, stored hashed (SHA-256) in `refresh_tokens` table.
  - **Refresh Token Rotation**: each use of a refresh token revokes it and issues a new one. If a revoked token is reused, all tokens for that user are immediately revoked.
  - **Refresh Token Cookie**: `HttpOnly` cookie named `refresh_token`, path `/api/v1/auth`.
- **Endpoints** (all prefixed with `/api/v1`):
  - `POST /api/v1/auth/register` — public, creates `USER` account
  - `POST /api/v1/auth/login` — public, returns access token + sets refresh cookie
  - `POST /api/v1/auth/refresh` — public, reads refresh cookie, rotates token, returns new access token
  - `POST /api/v1/auth/logout` — public, revokes refresh cookie
  - `GET /api/v1/categories` — authenticated, list all active categories (paginated)
  - `GET /api/v1/admin/users` — ADMIN, list all users (paginated, optional filters by `role` and `status`)
  - `GET /api/v1/admin/users/{id}` — ADMIN, get one user by ID
  - `PUT /api/v1/admin/users/{id}` — ADMIN, update user (name, email, role, status including BLOCKED)
  - `DELETE /api/v1/admin/users/{id}` — ADMIN, soft delete user
  - `GET /api/v1/admin/categories` — ADMIN, list all categories (paginated)
  - `POST /api/v1/admin/categories` — ADMIN, create category
  - `PUT /api/v1/admin/categories/{id}` — ADMIN, update category
  - `DELETE /api/v1/admin/categories/{id}` — ADMIN, soft delete category
  - `GET /api/v1/admin/security/brute-force/blocked` — ADMIN, list accounts blocked by brute-force protection (paginated, default sort `email,asc`)
  - `POST /api/v1/admin/security/brute-force/unblock` — ADMIN, manually unblock a brute-force locked account by email
  - `POST /api/v1/admin/security/refresh-tokens/cleanup` — ADMIN, physically delete revoked refresh tokens whose expiration date has passed
  - All other endpoints require valid JWT.
  - **Nota sobre `requestMatchers`**: los patrones en `SecurityConfig.java` NO incluyen el
    context-path (`/api/v1`). Spring Security los compara contra el servlet-path (sin
    context-path), por lo tanto usan rutas como `/auth/register` y no `/api/v1/auth/register`.

## Discrepancies to Watch

- Hibernate DDL auto is set to `${DDL_AUTO:update}` (defaults to `update` if env var is missing). This mutates the schema on startup.
