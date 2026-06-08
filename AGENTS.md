# AGENTS.md — mis-fichas

## Build & Run

- **Build tool**: Maven (`./mvnw`)
- **Java**: 17
- **Spring Boot**: 4.0.6 (per `pom.xml`)

## Database Dependency

- **MariaDB 11.8** is required for both the application and tests.
- **No embedded/H2 database** is configured. Tests fail without a live DB.
- Start the DB before running anything:
  ```bash
  docker compose up -d
  ```
- The container uses credentials from `.env` (mapped to `docker-compose.yml`).

## Environment Variables

- Spring Boot reads database config from **environment variables** loaded via `application.yaml`.
- The `.env` file at the repo root is **not automatically loaded** by Spring Boot.
- Export variables before running Maven:
  ```bash
  export $(grep -v '^#' .env | xargs)
  ```
- Then run:
  ```bash
  ./mvnw spring-boot:run
  ./mvnw test
  ```
- Note: `.env` is listed in `.gitignore` but is currently tracked in the repo.

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
- **Categories**:
  - *System categories* (`isSystem = true`): seeded automatically on application startup. Only an `ADMIN` can modify them.
  - *User categories*: `USER` role accounts can create and manage their own personal categories (basic CRUD).
- **Soft delete**: All main entities use `deletedAt` instead of physical deletion.

## Architecture Notes

- **Controllers**: `com.fcs.mis_fichas.controllers.*`
- **Entities**: `com.fcs.mis_fichas.entities.*`
- **Repositories**: `com.fcs.mis_fichas.repositories.*` (currently empty — project is in early development)
- **Services**: `com.fcs.mis_fichas.services.*` (currently empty)
- **Enums**: `com.fcs.mis_fichas.enums.*` (Role, Status, Type)

## Team Conventions

- No formal branching, commit message, or code style conventions (e.g. Spotless, Checkstyle) are currently defined.

## Database Migrations

- No decision yet on Flyway or Liquibase. Hibernate `ddl-auto: update` is the current approach and will remain for the near term.

## Authentication & Security

- `spring-boot-starter-security` is now in `pom.xml` (added to support `BCryptPasswordEncoder` and admin seeding).
- **Admin seeder**: `AdminSeeder` runs on startup and creates the default admin account if it does not exist:
  - Email: `admin@mis-fichas.fcs`
  - Password: `Admin123!` (hashed with BCrypt)
  - Role: `ADMIN`
- **Current security posture**: All endpoints are permit-all (`SecurityConfig` disables CSRF and allows any request). This is temporary until JWT is fully implemented.
- **Planned JWT design**: access tokens expire every 30 min; refresh tokens stored in `refresh_tokens` table expire after 30 days with active rotation (each use generates a new one and invalidates the previous). Auto-renewal without user intervention when possible.

## Discrepancies to Watch

- Hibernate DDL auto is set to `${DDL_AUTO:update}` (defaults to `update` if env var is missing). This mutates the schema on startup.
