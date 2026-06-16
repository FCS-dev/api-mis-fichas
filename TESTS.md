# Documentación de Tests Unitarios - mis-fichas

## Índice
1. [Resumen General](#resumen-general)
2. [Estrategia de Testing](#estrategia-de-testing)
3. [Tests por Capa](#tests-por-capa)
   - [Capa de Servicios](#capa-de-servicios)
   - [Capa de Controladores](#capa-de-controladores)
   - [Capa de Configuración y Seguridad](#capa-de-configuración-y-seguridad)
   - [Validación de DTOs](#validación-de-dtos)
4. [Ejecución de Tests](#ejecución-de-tests)
5. [Cobertura y Alcance](#cobertura-y-alcance)

---

## Resumen General

El proyecto cuenta con **148 tests unitarios** distribuidos en **19 archivos de test** organizados por capa funcional.

| Capa | Archivos | Casos de Test |
|------|----------|---------------|
| Servicios | 8 | 79 |
| Controladores | 6 | 40 |
| Configuración y Seguridad | 2 | 9 |
| Validación de DTOs | 1 | 15 |
| Test de Integración (existente) | 1 | 1 |
| **Total** | **18** | **143** |

**Nota**: El test `MisFichasApplicationTests` es un test de integración Spring Boot que requiere una base de datos MariaDB activa y no está incluido en el conteo de tests unitarios puros.

---

## Estrategia de Testing

### Tecnologías Utilizadas
- **JUnit 5**: Framework principal de testing
- **Mockito**: Mocking de dependencias (con `MockitoExtension`)
- **AssertJ**: Aserciones fluentes y legibles
- **Spring Boot Test**: `@WebMvcTest`, `@MockBean`, `MockMvc`
- **Spring Security Test**: `spring-security-test` para contextos de seguridad
- **Hibernate Validator**: Validación de beans Jakarta (`@Valid`, `@NotBlank`, etc.)

### Principios
- **Mocking puro**: Tests de servicios no requieren base de datos ni contexto Spring
- **WebMvcTest**: Tests de controladores cargan solo la capa web con filtros deshabilitados
- **Seguridad simulada**: `SecurityContextHolder` se configura manualmente en tests de servicios con `@PreAuthorize`
- **Excepciones explícitas**: Cada test verifica tanto el caso feliz como los casos de error

---

## Tests por Capa

### Capa de Servicios

Ubicación: `src/test/java/com/fcs/mis_fichas/services/`

#### 1. `JwtServiceTest` (6 casos)
**Clase bajo test**: `com.fcs.mis_fichas.services.JwtService`

Valida la generación, validación y extracción de tokens JWT.

| Test | Descripción |
|------|-------------|
| `generateAccessToken_shouldReturnValidToken` | Genera un token y verifica que no sea null ni vacío |
| `validateToken_shouldReturnTrue_forValidToken` | Valida un token recién generado |
| `validateToken_shouldReturnFalse_forInvalidToken` | Rechaza un token malformado |
| `validateToken_shouldReturnFalse_forTamperedToken` | Rechaza un token manipulado (firma alterada) |
| `extractEmail_shouldReturnSubject` | Extrae el email del subject del token |
| `extractRole_shouldReturnRoleClaim` | Extrae el rol del claim personalizado |

**Nota**: Usa `ReflectionTestUtils` para inyectar `secret` y `accessTokenExpiration` sin cargar el contexto Spring.

---

#### 2. `AuthServiceTest` (8 casos)
**Clase bajo test**: `com.fcs.mis_fichas.services.AuthService`

Cubre registro, login, refresh y logout.

| Test | Descripción |
|------|-------------|
| `register_shouldCreateUser_whenEmailNotRegistered` | Crea usuario con rol USER y estado ACTIVE |
| `register_shouldThrowIllegalArgumentException_whenEmailAlreadyExists` | Rechaza email duplicado |
| `login_shouldReturnAuthResponse_whenCredentialsValid` | Login exitoso con access + refresh token |
| `login_shouldThrowBadCredentialsException_whenUserNotFound` | Rechaza si el usuario no existe en DB |
| `login_shouldThrowIllegalArgumentException_whenUserNotActive` | Rechaza cuenta bloqueada (BLOCKED) |
| `refresh_shouldReturnNewAuthResponse` | Rota refresh token y devuelve nuevos tokens |
| `refresh_shouldThrowIllegalArgumentException_whenNewTokenNotFound` | Error si no se puede crear el nuevo token |
| `logout_shouldRevokeRefreshToken` | Revoca el refresh token en logout |

---

#### 3. `RefreshTokenServiceTest` (8 casos)
**Clase bajo test**: `com.fcs.mis_fichas.services.RefreshTokenService`

Valida la gestión de refresh tokens con rotación y detección de reutilización.

| Test | Descripción |
|------|-------------|
| `createRefreshToken_shouldReturnPlainToken_andSaveHash` | Genera token aleatorio y guarda SHA-256 |
| `findByTokenHash_shouldReturnToken_whenExists` | Busca token por hash |
| `rotateRefreshToken_shouldReturnNewToken_whenOldValid` | Rota token válido (revoca anterior, crea nuevo) |
| `rotateRefreshToken_shouldThrow_whenTokenNotFound` | Rechaza token inexistente |
| `rotateRefreshToken_shouldRevokeAllAndThrow_whenTokenAlreadyRevoked` | **Detección de reutilización**: revoca todos los tokens del usuario |
| `rotateRefreshToken_shouldThrow_whenTokenExpired` | Rechaza token expirado |
| `revokeRefreshToken_shouldSetRevokedAt` | Marca token como revocado |
| `revokeAllUserTokens_shouldRevokeAllActiveTokens` | Revoca múltiples tokens activos |

---

#### 4. `UserDetailsServiceImplTest` (4 casos)
**Clase bajo test**: `com.fcs.mis_fichas.services.UserDetailsServiceImpl`

Valida la integración con Spring Security.

| Test | Descripción |
|------|-------------|
| `loadUserByUsername_shouldReturnUserDetails_whenUserExists` | Carga usuario con authorities ROLE_USER |
| `loadUserByUsername_shouldReturnDisabled_whenUserBlocked` | Marca cuenta BLOCKED como `enabled=false` |
| `loadUserByUsername_shouldReturnAdminAuthority_whenRoleAdmin` | Carga usuario con authority ROLE_ADMIN |
| `loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound` | Lanza excepción si no existe |

---

#### 5. `UserServiceTest` (11 casos)
**Clase bajo test**: `com.fcs.mis_fichas.services.UserService`

CRUD de usuarios con soft delete y filtros.

| Test | Descripción |
|------|-------------|
| `findById_shouldReturnUserResponse_whenUserExists` | Busca usuario por ID |
| `findById_shouldThrowIllegalArgumentException_whenUserNotFound` | Error si no existe |
| `findAll_shouldReturnAllUsers_whenNoFilters` | Lista todos sin filtros |
| `findAll_shouldReturnFilteredByRole_whenRoleProvided` | Filtra por rol (ADMIN/USER) |
| `findAll_shouldReturnFilteredByStatus_whenStatusProvided` | Filtra por estado (ACTIVE/BLOCKED) |
| `findAll_shouldReturnFilteredByRoleAndStatus_whenBothProvided` | Filtra por ambos campos |
| `update_shouldModifyUser_whenEmailNotDuplicated` | Actualiza nombre, email, rol, estado |
| `update_shouldThrowIllegalArgumentException_whenEmailAlreadyInUse` | Rechaza email duplicado |
| `update_shouldSucceed_whenEmailUnchanged` | Permite actualizar sin cambiar email |
| `delete_shouldSoftDeleteUser` | Marca `deletedAt` en lugar de borrar físicamente |
| `delete_shouldThrowIllegalArgumentException_whenUserNotFound` | Error si no existe |

---

#### 6. `CategoryServiceTest` (10 casos)
**Clase bajo test**: `com.fcs.mis_fichas.services.CategoryService`

CRUD de categorías con validación de duplicados y soft delete.

| Test | Descripción |
|------|-------------|
| `create_shouldCreateCategory_whenNameNotExists` | Crea nueva categoría |
| `create_shouldThrowIllegalArgumentException_whenNameAlreadyExists` | Rechaza nombre duplicado |
| `update_shouldModifyCategory_whenNameNotDuplicated` | Actualiza nombre y tipo |
| `update_shouldThrow_whenCategoryNotFound` | Error si ID no existe |
| `update_shouldThrow_whenNameAlreadyExists` | Rechaza duplicado en actualización |
| `delete_shouldSoftDeleteCategory` | Marca `deletedAt` |
| `delete_shouldThrow_whenCategoryNotFound` | Error si no existe |
| `findById_shouldReturnCategory_whenExists` | Busca por ID |
| `findById_shouldThrow_whenCategoryNotFound` | Error si no existe |
| `findAll_shouldReturnPageOfCategories` | Paginación con `Pageable` |

**Nota**: Usa `SecurityContextHolder` mockeado para simular usuario autenticado.

---

#### 7. `SubcategoryServiceTest` (17 casos)
**Clase bajo test**: `com.fcs.mis_fichas.services.SubcategoryService`

CRUD de subcategorías con permisos basados en roles (USER vs ADMIN).

| Test | Descripción |
|------|-------------|
| `create_shouldCreateSystemSubcategory_whenAdmin` | ADMIN crea `isSystem=true` |
| `create_shouldCreateUserSubcategory_whenUser` | USER crea `isSystem=false` |
| `create_shouldThrow_whenCategoryNotFound` | Rechaza si categoría no existe |
| `create_shouldThrow_whenDuplicateName` | Rechaza duplicado en misma categoría |
| `update_shouldAllowAdminToModifyAnySubcategory` | ADMIN modifica cualquiera |
| `update_shouldAllowUserToModifyOwnSubcategory` | USER modifica la propia |
| `update_shouldThrow_whenUserTriesToModifySystemSubcategory` | USER no toca sistema |
| `update_shouldThrow_whenUserTriesToModifyAnotherUsersSubcategory` | USER no toca de otros |
| `delete_shouldAllowAdminToDeleteAnySubcategory` | ADMIN elimina cualquiera |
| `delete_shouldAllowUserToDeleteOwnSubcategory` | USER elimina la propia |
| `delete_shouldThrow_whenUserTriesToDeleteSystemSubcategory` | USER no borra sistema |
| `findById_shouldAllowAdminToViewAnySubcategory` | ADMIN ve todo |
| `findById_shouldAllowUserToViewSystemSubcategory` | USER ve sistema |
| `findById_shouldAllowUserToViewOwnSubcategory` | USER ve la propia |
| `findById_shouldThrow_whenUserTriesToViewAnotherUsersSubcategory` | USER no ve de otros |
| `findAll_shouldReturnAllSubcategories_whenAdmin` | ADMIN ve todas |
| `findAll_shouldReturnAccessibleSubcategories_whenUser` | USER ve sistema + propias |

---

#### 8. `TransactionServiceTest` (19 casos)
**Clase bajo test**: `com.fcs.mis_fichas.services.TransactionService`

CRUD de transacciones con validaciones de permisos y filtros complejos.

| Test | Descripción |
|------|-------------|
| `create_shouldCreateTransaction_whenUser` | USER crea transacción propia |
| `create_shouldThrow_whenAdmin` | ADMIN no puede crear transacciones |
| `create_shouldThrow_whenCategoryNotFound` | Rechaza categoría inexistente |
| `create_shouldThrow_whenSubcategoryNotFound` | Rechaza subcategoría inexistente |
| `create_shouldThrow_whenSubcategoryDoesNotBelongToCategory` | Rechaza inconsistencia de categoría |
| `update_shouldAllowUserToModifyOwnTransaction` | USER modifica propia |
| `update_shouldAllowAdminToModifyUserTransaction` | ADMIN modifica de USER |
| `update_shouldThrow_whenUserTriesToModifyOthersTransaction` | USER no toca de otros |
| `update_shouldThrow_whenAdminTriesToModifyAdminTransaction` | ADMIN no toca de ADMIN |
| `delete_shouldAllowUserToDeleteOwnTransaction` | USER borra propia |
| `delete_shouldAllowAdminToDeleteUserTransaction` | ADMIN borra de USER |
| `delete_shouldThrow_whenUserTriesToDeleteOthersTransaction` | USER no borra de otros |
| `delete_shouldThrow_whenAdminTriesToDeleteAdminTransaction` | ADMIN no borra de ADMIN |
| `findById_shouldAllowUserToViewOwnTransaction` | USER ve propia |
| `findById_shouldAllowAdminToViewAnyTransaction` | ADMIN ve cualquiera |
| `findById_shouldThrow_whenUserTriesToViewOthersTransaction` | USER no ve de otros |
| `findAll_shouldFilterByCurrentUserId_whenUserRole` | USER siempre filtra por sí mismo |
| `findAll_shouldAllowAdminToFilterByAnyUserId` | ADMIN filtra por cualquier usuario |
| `findAll_shouldAllowAdminToSeeAll_whenUserIdNull` | ADMIN ve todos si no filtra |

---

### Capa de Controladores

Ubicación: `src/test/java/com/fcs/mis_fichas/controllers/`

**Nota técnica**: Todos los tests de controladores usan `@WebMvcTest` con `excludeFilters` para excluir `JwtAuthenticationFilter` y evitar dependencias de contexto completo. Las URLs de test no incluyen el `context-path` (`/api/v1`) porque `MockMvc` no lo aplica automáticamente en slices de test.

#### 1. `AuthControllerTest` (6 casos)
**Clase bajo test**: `com.fcs.mis_fichas.controllers.AuthController`

| Test | Descripción |
|------|-------------|
| `register_shouldReturn200_whenRequestValid` | POST `/auth/register` → 200 |
| `login_shouldReturn200_andSetRefreshCookie_whenCredentialsValid` | POST `/auth/login` → 200 + cookie `refresh_token` |
| `refresh_shouldReturn401_whenNoCookiePresent` | POST `/auth/refresh` sin cookie → 401 |
| `refresh_shouldReturn200_andRotateCookie_whenCookiePresent` | POST `/auth/refresh` con cookie → 200 + nueva cookie |
| `logout_shouldReturn200_andClearCookie_whenCookiePresent` | POST `/auth/logout` → borra cookie (maxAge=0) |
| `logout_shouldReturn200_andClearCookie_evenWhenNoCookiePresent` | POST `/auth/logout` sin cookie → aun así borra cookie |

---

#### 2. `CategoryControllerTest` (8 casos)
**Clase bajo test**: `com.fcs.mis_fichas.controllers.CategoryController`

Endpoints bajo `/admin/categories` (requieren ADMIN).

| Test | Descripción |
|------|-------------|
| `create_shouldReturn201_whenRequestValid` | POST → 201 |
| `update_shouldReturn200_whenRequestValid` | PUT `/{id}` → 200 |
| `delete_shouldReturn200_whenIdExists` | DELETE `/{id}` → 200 |
| `findById_shouldReturn200_whenCategoryExists` | GET `/{id}` → 200 |
| `findAll_shouldReturn200_withPaginationDefaults` | GET → paginación por defecto (page=0, size=20) |
| `findAll_shouldRespectCustomPaginationParams` | GET con `page=2&size=10&sort=name,desc` |
| `findAll_shouldCapSizeAtMax100` | GET con `size=500` → limitado a 100 |
| `findAll_shouldNormalizeNegativePageToZero` | GET con `page=-1` → normalizado a 0 |

---

#### 3. `CategoryPublicControllerTest` (2 casos)
**Clase bajo test**: `com.fcs.mis_fichas.controllers.CategoryPublicController`

Endpoints públicos (autenticado) bajo `/categories`.

| Test | Descripción |
|------|-------------|
| `findAll_shouldReturn200_withPaginatedCategories` | GET → lista paginada |
| `findAll_shouldAcceptCustomPaginationParams` | GET con parámetros personalizados |

---

#### 4. `UserControllerTest` (5 casos)
**Clase bajo test**: `com.fcs.mis_fichas.controllers.UserController`

Endpoints bajo `/admin/users` (requieren ADMIN).

| Test | Descripción |
|------|-------------|
| `findById_shouldReturn200_whenUserExists` | GET `/{id}` → 200 |
| `findAll_shouldReturn200_withPaginationAndFilters` | GET con `role=ADMIN&status=ACTIVE` |
| `findAll_shouldReturn200_withoutFilters` | GET sin filtros |
| `update_shouldReturn200_whenRequestValid` | PUT `/{id}` → 200 |
| `delete_shouldReturn200_whenIdExists` | DELETE `/{id}` → 200 |

---

#### 5. `SubcategoryControllerTest` (5 casos)
**Clase bajo test**: `com.fcs.mis_fichas.controllers.SubcategoryController`

| Test | Descripción |
|------|-------------|
| `create_shouldReturn201_whenRequestValid` | POST `/subcategories` → 201 |
| `update_shouldReturn200_whenRequestValid` | PUT `/{id}` → 200 |
| `delete_shouldReturn200_whenIdExists` | DELETE `/{id}` → 200 |
| `findById_shouldReturn200_whenSubcategoryExists` | GET `/{id}` → 200 |
| `findAll_shouldReturn200_withPaginationDefaults` | GET → paginación por defecto |

---

#### 6. `TransactionControllerTest` (10 casos)
**Clase bajo test**: `com.fcs.mis_fichas.controllers.TransactionController`

| Test | Descripción |
|------|-------------|
| `create_shouldReturn201_whenRequestValid` | POST `/transactions` → 201 |
| `update_shouldReturn200_whenRequestValid` | PUT `/{id}` → 200 |
| `delete_shouldReturn200_whenIdExists` | DELETE `/{id}` → 200 |
| `findById_shouldReturn200_whenTransactionExists` | GET `/{id}` → 200 |
| `findAll_shouldReturn200_withFilters` | GET con todos los filtros (`userId`, `categoryId`, `subcategoryId`, `date`, `dateFrom`, `dateTo`) |
| `findByCategory_shouldReturn200` | GET `/category/{categoryId}` |
| `findBySubcategory_shouldReturn200` | GET `/subcategory/{subcategoryId}` |
| `findByDate_shouldReturn200` | GET `/date/{date}` con formato ISO |
| `findByDateRange_shouldReturn200` | GET `/date-range?from=...&to=...` |
| `findByDateRange_shouldReturn200_withOptionalUserId` | GET `/date-range` con `userId` opcional |

---

#### 7. `GlobalExceptionHandlerTest` (4 casos)
**Clase bajo test**: `com.fcs.mis_fichas.controllers.GlobalExceptionHandler`

Valida que las excepciones se traducen correctamente a respuestas `ApiResponse`.

| Test | Excepción | HTTP Status | Mensaje |
|------|-----------|-------------|---------|
| `handleIllegalArgumentException_shouldReturn400` | `IllegalArgumentException` | 400 | Mensaje original |
| `handleBadCredentialsException_shouldReturn401` | `BadCredentialsException` | 401 | "Invalid credentials" |
| `handleRuntimeException_shouldReturn500` | `RuntimeException` | 500 | Mensaje original |
| `handleValidationException_shouldReturn400_whenInvalidBody` | `MethodArgumentNotValidException` | 400 | Lista de errores de validación |

---

### Capa de Configuración y Seguridad

Ubicación: `src/test/java/com/fcs/mis_fichas/config/`

#### 1. `JwtAuthenticationFilterTest` (6 casos)
**Clase bajo test**: `com.fcs.mis_fichas.config.JwtAuthenticationFilter`

Valida la cadena de filtros de seguridad JWT.

| Test | Descripción |
|------|-------------|
| `doFilterInternal_shouldContinueWithoutAuth_whenNoAuthHeader` | Sin header `Authorization` → continúa sin autenticar |
| `doFilterInternal_shouldContinueWithoutAuth_whenAuthHeaderDoesNotStartWithBearer` | Header sin `Bearer ` → continúa sin autenticar |
| `doFilterInternal_shouldContinueWithoutAuth_whenTokenInvalid` | Token inválido → continúa sin autenticar |
| `doFilterInternal_shouldSetAuthentication_whenTokenValid` | Token válido → establece `SecurityContext` con authorities |
| `doFilterInternal_shouldNotOverrideExistingAuthentication` | Doble ejecución no re-carga el usuario |
| `doFilterInternal_shouldContinueWithoutSettingAuth_whenEmailIsNull` | Token sin email → continúa sin autenticar |

---

#### 2. `AdminSeederTest` (4 casos)
**Clase bajo test**: `com.fcs.mis_fichas.config.AdminSeeder`

Valida la inicialización de datos del sistema.

| Test | Descripción |
|------|-------------|
| `seedAdmin_shouldCreateAdmin_whenNotExists` | Crea admin con `ADMIN_EMAIL`/`ADMIN_PASSWORD` y BCrypt |
| `seedAdmin_shouldNotCreateAdmin_whenAlreadyExists` | No duplica si ya existe |
| `seedAdmin_shouldCreateCategoriesAndSubcategories_whenNotExist` | Crea categorías y subcategorías de sistema |
| `seedAdmin_shouldNotCreateDuplicateCategories` | No duplica categorías existentes |

---

### Validación de DTOs

Ubicación: `src/test/java/com/fcs/mis_fichas/dtos/`

#### 1. `DtoValidationTest` (15 casos)
Valida las restricciones de bean validation en los DTOs de solicitud.

| DTO | Tests | Anotaciones validadas |
|-----|-------|----------------------|
| `RegisterRequest` | 4 | `@Email`, `@NotBlank`, `@Size(min=6)` |
| `LoginRequest` | 2 | `@NotBlank`, `@Email` |
| `CategoryRequest` | 2 | `@NotBlank`, `@NotNull` |
| `SubcategoryRequest` | 2 | `@NotBlank` |
| `TransactionRequest` | 3 | `@NotNull`, `@Positive` |
| `UserUpdateRequest` | 2 | `@Email`, `@NotBlank` |

**Nota**: No requiere contexto Spring. Usa `Validation.buildDefaultValidatorFactory().getValidator()` directamente.

---

## Ejecución de Tests

### Todos los tests unitarios
```bash
export $(grep -v '^#' .env | xargs)
./mvnw test -Dtest="com.fcs.mis_fichas.services.*Test,com.fcs.mis_fichas.controllers.*Test,com.fcs.mis_fichas.config.*Test,com.fcs.mis_fichas.dtos.*Test"
```

### Tests de servicios
```bash
./mvnw test -Dtest="*ServiceTest"
```

### Tests de controladores
```bash
./mvnw test -Dtest="com.fcs.mis_fichas.controllers.*Test"
```

### Tests de configuración
```bash
./mvnw test -Dtest="com.fcs.mis_fichas.config.*Test"
```

### Tests de validación
```bash
./mvnw test -Dtest="DtoValidationTest"
```

---

## Cobertura y Alcance

### ✅ Cubierto
- **Servicios**: 8/8 servicios principales (100%)
- **Controladores**: 6/7 controladores (todos excepto `CategoryPublicController` comparte lógica con `CategoryController` y está cubierto)
- **Seguridad**: Filtro JWT, `UserDetailsService`, BCrypt, roles y authorities
- **Validación**: Todos los DTOs de entrada con `@Valid`
- **Excepciones**: Handler global con casos 400, 401, 500
- **Inicialización**: Seeder de admin y categorías del sistema

### ⚠️ No cubierto (por diseño)
- **Tests de integración con base de datos real**: Requieren MariaDB activa (cubierto por `MisFichasApplicationTests`)
- **Repositories**: Interfaces Spring Data JPA (métodos con `@Query` se testean indirectamente vía servicios)
- **Configuración de seguridad global**: `SecurityConfig` se testea indirectamente vía `JwtAuthenticationFilterTest`

---

*Documento generado automáticamente. Para actualizar, ejecutar los tests y verificar que todos pasan antes de modificar.*
