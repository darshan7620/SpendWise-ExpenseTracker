# Phase 4 Completion Report - SpendWise

## 1. Phase Summary

- **Phase number and name:** Phase 4 - Spring Security, JWT Authentication, Role-Based Authorization, and User Ownership Enforcement
- **Objective:** Secure the REST API with stateless JWT authentication, BCrypt password hashing, registration/login endpoints, role-based access control, and reliable per-user ownership enforcement (replacing the Phase 3 placeholder user).
- **Status:** COMPLETED
- **Brief summary of actual work performed:** Repaired a corrupted `pom.xml` (left over from an aborted start of this phase) and added the missing H2 test dependency; implemented the full Spring Security + JWT stack; added auth DTOs, service, controller, and an ADMIN-only endpoint; replaced the Phase 3 `DefaultCurrentUserProvider` stub with a real `SecurityContextCurrentUserProvider`; added `ForbiddenException`/`UnauthorizedException` and consistent 401/403 handling; enforced ownership on read/write endpoints; and added an end-to-end auth integration test. `mvnw clean compile` and `mvnw test` (6 tests) pass.

> **Note on prior state:** Phase 4 had not been implemented. Only the `spring-boot-starter-security` starter had been added and the JJWT dependencies had been appended *after* `</build>` (outside `<dependencies>`) with a duplicate `</project>`, which made the POM non-parseable and the backend unbuildable. This was corrected first.

## 2. Implementation Details

**Fully implemented features:**
- Stateless Spring Security filter chain (`SessionCreationPolicy.STATELESS`), CSRF disabled, method security enabled (`@EnableMethodSecurity`)
- JWT generation/validation using JJWT 0.12.3 (subject = email, claims `userId` and `role`, configurable secret and expiration)
- `JwtAuthenticationFilter` (`OncePerRequestFilter`) reads the `Bearer` token, loads the user, and populates the `SecurityContext`
- `CustomUserDetailsService` + `UserPrincipal` (`UserDetails`) mapping the `User` entity and `ROLE_<role>` authorities
- `BCryptPasswordEncoder` password hashing
- `AuthenticationManager` wired through `AuthenticationConfiguration`, used by login
- Public endpoints: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`; everything else authenticated
- Authenticated endpoint: `GET /api/v1/auth/me`
- Role-based authorization: `/api/v1/admin/**` requires `ROLE_ADMIN` (both URL rule and `@PreAuthorize`); `AdminController` lists users
- User ownership enforcement: replaced hardcoded demo-user provider with `SecurityContextCurrentUserProvider`; added per-user scoping on all reads and `ForbiddenException` (403) on cross-user access; fixed `CategoryServiceImpl.getAll` to return only system + current user's categories
- Consistent error responses: `JwtAuthenticationEntryPoint` (401), `JwtAccessDeniedHandler` (403), and `GlobalExceptionHandler` handlers for `ForbiddenException`, `UnauthorizedException`, `AccessDeniedException`, and `AuthenticationException`
- CORS configuration for the React dev origins (`http://localhost:5173`, `http://localhost:3000`)

**Partially implemented:** None.

**Not implemented (out of scope):** React/frontend login UI, token storage/interceptors, refresh tokens, password reset, dashboard/reports.

## 3. Files Created, Modified, Deleted

| File path | Action | Purpose |
|---|---|---|
| `backend/pom.xml` | MODIFIED | Repaired malformed POM; moved JJWT deps into `<dependencies>`; parametrized `jjwt.version`; restored missing `h2` test dependency. |
| `backend/src/main/resources/application.properties` | MODIFIED | Added `jwt.secret`, `jwt.expiration-ms`, `app.cors.allowed-origins`. |
| `backend/src/main/java/com/spendwise/security/JwtProperties.java` | CREATED | Binds `jwt.*` configuration. |
| `backend/src/main/java/com/spendwise/security/JwtService.java` | CREATED | Token generation and validation (JJWT). |
| `backend/src/main/java/com/spendwise/security/UserPrincipal.java` | CREATED | `UserDetails` adapter over `User`. |
| `backend/src/main/java/com/spendwise/security/CustomUserDetailsService.java` | CREATED | Loads users by email. |
| `backend/src/main/java/com/spendwise/security/JwtAuthenticationFilter.java` | CREATED | Bearer-token authentication filter. |
| `backend/src/main/java/com/spendwise/security/JwtAuthenticationEntryPoint.java` | CREATED | JSON 401 response. |
| `backend/src/main/java/com/spendwise/security/JwtAccessDeniedHandler.java` | CREATED | JSON 403 response. |
| `backend/src/main/java/com/spendwise/security/SecurityConfig.java` | CREATED | Filter chain, BCrypt, AuthenticationManager, method security, CORS. |
| `backend/src/main/java/com/spendwise/dto/RegisterRequest.java` | CREATED | Registration payload with validation. |
| `backend/src/main/java/com/spendwise/dto/LoginRequest.java` | CREATED | Login payload with validation. |
| `backend/src/main/java/com/spendwise/dto/AuthResponse.java` | CREATED | Access token + user summary. |
| `backend/src/main/java/com/spendwise/dto/UserResponse.java` | CREATED | Safe user projection (no password). |
| `backend/src/main/java/com/spendwise/mapper/UserMapper.java` | CREATED | `User` -> `UserResponse`. |
| `backend/src/main/java/com/spendwise/service/AuthService.java` | CREATED | Auth service interface. |
| `backend/src/main/java/com/spendwise/service/impl/AuthServiceImpl.java` | CREATED | Register/login/current-user logic. |
| `backend/src/main/java/com/spendwise/controller/AuthController.java` | CREATED | `/api/v1/auth/*` endpoints. |
| `backend/src/main/java/com/spendwise/controller/AdminController.java` | CREATED | ADMIN-only user listing. |
| `backend/src/main/java/com/spendwise/exception/ForbiddenException.java` | CREATED | 403 ownership violation. |
| `backend/src/main/java/com/spendwise/exception/UnauthorizedException.java` | CREATED | 401 auth failure. |
| `backend/src/main/java/com/spendwise/exception/GlobalExceptionHandler.java` | MODIFIED | Added 401/403 handlers. |
| `backend/src/main/java/com/spendwise/service/impl/SecurityContextCurrentUserProvider.java` | CREATED | Real current-user provider. |
| `backend/src/main/java/com/spendwise/service/impl/DefaultCurrentUserProvider.java` | DELETED | Removed Phase 3 demo-user stub. |
| `backend/src/main/java/com/spendwise/repository/CategoryRepository.java` | MODIFIED | Added owner-scoped paged query. |
| `backend/src/main/java/com/spendwise/service/impl/CategoryServiceImpl.java` | MODIFIED | Owner-scoped listing; ownership checks; 403. |
| `backend/src/main/java/com/spendwise/service/impl/TransactionServiceImpl.java` | MODIFIED | Ownership checks on read/update/delete; 403. |
| `backend/src/main/java/com/spendwise/service/impl/BudgetServiceImpl.java` | MODIFIED | Ownership checks on read/update/delete; 403. |
| `backend/src/main/java/com/spendwise/service/impl/SavingsGoalServiceImpl.java` | MODIFIED | Ownership checks on read/update/progress/delete; 403. |
| `backend/src/test/java/com/spendwise/AuthFlowIntegrationTest.java` | CREATED | End-to-end auth/authz tests (5). |
| `backend/src/test/java/com/spendwise/SpendWiseApplicationTests.java` | MODIFIED | H2 `NON_KEYWORDS=MONTH,YEAR` so reserved column names work. |

## 4. Dependencies and Configuration

**New/changed dependencies (Maven):**
- `io.jsonwebtoken:jjwt-api:0.12.3` (compile), `jjwt-impl:0.12.3` (runtime), `jjwt-jackson:0.12.3` (runtime)
- `spring-boot-starter-security` (already present, now actually used)
- `com.h2database:h2` (test) restored
- Spring Boot 4.0.0 continues to use Jackson 3 (`tools.jackson`) for JSON; security handlers intentionally avoid direct Jackson coupling.

**Configuration:**
- `jwt.secret` (env `JWT_SECRET`, default dev value), `jwt.expiration-ms` (env `JWT_EXPIRATION_MS`, default 24h)
- `app.cors.allowed-origins` (env `CORS_ALLOWED_ORIGINS`, default `http://localhost:5173,http://localhost:3000`)

**Environment variables:**
- `JWT_SECRET` (must be >= 32 bytes; override in production)
- `JWT_EXPIRATION_MS` (optional)
- `CORS_ALLOWED_ORIGINS` (optional)
- Existing `DB_USERNAME` / `DB_PASSWORD` unchanged

## 5. Verification and Test Results

Commands executed:

1. `cd backend && .\mvnw.cmd clean compile` — **PASS**, BUILD SUCCESS
2. `cd backend && .\mvnw.cmd test` — **PASS**, BUILD SUCCESS
   - Tests run: 6, Passed: 6, Failed: 0, Skipped: 0
   - `SpendWiseApplicationTests.contextLoads()` — PASS
   - `AuthFlowIntegrationTest` (5) — PASS:
     - register then login, access protected resource, and `/me`
     - duplicate registration -> 400
     - invalid login -> 401
     - admin endpoint as USER -> 403
     - malformed token -> 401

## 6. API and Database Changes

**New endpoints:**
- `POST /api/v1/auth/register` — create USER account, returns JWT (201)
- `POST /api/v1/auth/login` — authenticate, returns JWT (200)
- `GET /api/v1/auth/me` — current authenticated user (200)
- `GET /api/v1/admin/users` — list users, ADMIN only (200/403)

**Security behavior:** all existing `/api/v1/categories|transactions|budgets|goals` endpoints now require a valid JWT and are scoped to the authenticated user.

**Database:** no schema/entity changes. Passwords are now BCrypt-hashed when created through registration (the removed demo provider previously stored plaintext). Existing plaintext rows, if any, would need reseeding.

## 7. Outstanding Issues

- No refresh-token/rotation or token revocation (stateless logout only).
- Access tokens are stored client-side (frontend phase will decide the mechanism).
- `JwtAuthenticationFilter` is a `@Component`; it is also added to the chain explicitly. `OncePerRequestFilter` guards against double execution, so behavior is correct, but it could optionally be registered via `FilterRegistrationBean` to be fully explicit.

## 8. Acceptance Criteria Checklist

- [x] Spring Security configured (stateless filter chain, CSRF off, method security)
- [x] JWT authentication implemented (generation, validation, filter)
- [x] Registration and login endpoints implemented with validation
- [x] Passwords hashed with BCrypt
- [x] Role-based authorization enforced (ADMIN-only endpoint + URL rule)
- [x] User ownership enforced across read and write operations for all resources
- [x] Current-user provider replaced (demo stub removed)
- [x] Consistent 401/403 error responses
- [x] POM repaired; backend builds and all tests pass

## 9. Final Assessment

- **Overall phase status:** COMPLETED
- **Verification confidence and limitations:** High. Build and 6 automated tests pass, covering authentication, authorization, and error paths. Tests use H2; full MySQL runtime behavior (e.g., existing plaintext accounts) was not exercised.
- **Safe to proceed to next phase?** Yes.
- **Next phase:** Phase 5 - React frontend (routing, auth context/token handling, API client with interceptors, and CRUD UI for categories, transactions, budgets, and savings goals).
