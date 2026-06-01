# project_details.md

## Project Overview

Project name: FlyingBird (Crypto Dashboard)

Main project folder:

```text
D:\Bhavin
```

Frontend application:

```text
D:\Bhavin\FlyingBird-UI
```

Backend application:

```text
D:\Bhavin\Backend-Java\crypto
```

Current stack:

- Frontend: React 19, TypeScript, Vite 8
- Backend: Java 21, Spring Boot 4.0.5, Maven
- Database: MySQL (fly_db)
- Frontend package manager: npm
- Backend build tool: Maven (mvnw wrapper present)

## Important Instruction Files

```text
D:\Bhavin\CLAUDE.md
D:\Bhavin\instructions\workflow_instructions.md
D:\Bhavin\instructions\frontend_instructions.md
D:\Bhavin\instructions\backend_instructions.md
```

Claude must read this file first before scanning project files.

## Folder Structure

```text
D:\Bhavin
  CLAUDE.md
  project_details.md
  instructions/
    workflow_instructions.md
    frontend_instructions.md
    backend_instructions.md
  FlyingBird-UI/               React frontend
    src/
      assets/
      components/
        FlyingBirdLogo.tsx
        Navbar.tsx
        ProtectedRoute.tsx
      config/
        index.ts               exports BASE_URL from VITE_API_BASE_URL
      context/
        AuthContext.tsx
        ToastContext.tsx
      pages/
        LoginPage.tsx
        DashboardPage.tsx
        dashboard/
          OverviewPage.tsx
          JobsDetailsPage.tsx
          TradesPage.tsx
          HistoryPage.tsx
          AnalyticsPage.tsx
      services/
        api.ts                 axios instance with JWT interceptor
        authService.ts         login / logout / isTokenValid
      types/
        auth.ts                LoginRequestDto, AuthResponseDto, User, ErrorResponseDto
      App.tsx
      main.tsx
    .env                       (contains VITE_API_BASE_URL — do not commit)
    .env.example
    package.json
    vite.config.ts
  Backend-Java/
    crypto/                    active Spring Boot module
      src/
        main/
          java/com/flyingbird/crypto/
            config/
              SecurityConfig.java
              JwtAuthenticationFilter.java
              CustomAuthenticationEntryPoint.java
              CustomUserDetailsService.java
            controller/
              AuthController.java
              UserController.java
              AdminUtilityController.java
            dto/
              LoginRequestDto.java
              RegisterRequestDto.java
              AuthResponseDto.java
              UserDetailsRequestDto.java
              UserDetailsResponseDto.java
              ErrorResponse.java
            entity/
              User.java
            exception/
              GlobalExceptionHandler.java
              InvalidCredentialsException.java
              UserAlreadyExistsException.java
              UserNotFoundException.java
              ForbiddenAccessException.java
            repository/
              UserRepository.java
            service/
              AuthService.java / AuthServiceImpl.java
              UserService.java / UserServiceImpl.java
            util/
          resources/
            application.yaml
        test/
      pom.xml
      mvnw / mvnw.cmd
```

## Frontend Details

Frontend path:

```text
D:\Bhavin\FlyingBird-UI
```

- Framework: React 19.2
- Build tool: Vite 8.0
- Language: TypeScript 6.0
- Package manager: npm

### Frontend Commands

```cmd
cd D:\Bhavin\FlyingBird-UI
npm install
npm run dev
npm run build
npm run lint
npm run preview
```

Note: no `npm run test` script defined in package.json.

## Frontend Routes

| Route | Purpose | Main Files |
|---|---|---|
| `/login` | Login page | `pages/LoginPage.tsx` |
| `/dashboard` (index) | Overview tab | `pages/dashboard/OverviewPage.tsx` |
| `/dashboard/jobs-details` | Jobs details tab | `pages/dashboard/JobsDetailsPage.tsx` |
| `/dashboard/trades` | Trades tab | `pages/dashboard/TradesPage.tsx` |
| `/dashboard/history` | History tab | `pages/dashboard/HistoryPage.tsx` |
| `/dashboard/analytics` | Analytics tab | `pages/dashboard/AnalyticsPage.tsx` |
| `*` | Catch-all redirect | `App.tsx` → redirects to `/login` |

`/dashboard` and all sub-routes are wrapped in `ProtectedRoute` (requires valid JWT).

## Frontend Patterns

| Area | Current Pattern |
|---|---|
| Routing | `react-router-dom` v7, `BrowserRouter` + nested `Routes` |
| API calls | `axios` instance in `services/api.ts`; Bearer token injected via request interceptor |
| State management | React Context only (`AuthContext`, `ToastContext`) — no Redux or Zustand |
| Styling | Plain CSS (`App.css`, `index.css`); `lucide-react` icons |
| Forms | Controlled inputs via `useState`; client-side required-field validation (non-blank); `noValidate` on form element |
| Auth handling | Only `token` stored in `localStorage`; `isAuthenticated` is React state (initialised from `isTokenValid()`), updated via `setAuth`/`logout`; `userDetails` held in `AuthContext` state, fetched in background after login and on page refresh; 401 interceptor removes `token` and redirects to `/login?error=...` |
| Toasts | `ToastContext` provides `showSuccess(msg)` / `showError(msg)`; auto-dismiss after 5 s; manually dismissible; no backend calls |

## Backend Details

Backend path:

```text
D:\Bhavin\Backend-Java\crypto
```

- Framework: Spring Boot 4.0.5
- Java version: 21
- Build tool: Maven (wrapper: `mvnw.cmd`)
- App name: crypto
- Group ID: com.flyingbird
- Artifact ID: crypto

### Backend Commands

```cmd
cd D:\Bhavin\Backend-Java\crypto
mvnw.cmd clean install
mvnw.cmd spring-boot:run
mvnw.cmd test
```

## Backend API Endpoints

| Method | Endpoint | Auth | Purpose | Controller |
|---|---|---|---|---|
| POST | `/api/auth/login` | Public | Authenticate user, return JWT | `AuthController` |
| POST | `/api/auth/register` | Public | Register new user | `AuthController` |
| POST | `/api/users/userDetails` | JWT required | Get user details by username | `UserController` (path changed from `/users` to `/api/users` on 2026-06-01) |
| GET | `/protected-test` | JWT required | Test JWT authentication | `AdminUtilityController` |
| GET | `/admin-data` | JWT + ADMIN role | Admin-only data | `AdminUtilityController` |
| POST | `/admin/update-role` | JWT + ADMIN role | Update user role (admin only) | `AdminUtilityController` |
| GET | `/actuator/health` | Public | Health check | Spring Actuator |
| GET | `/swagger-ui/**` | Public | Swagger UI | springdoc-openapi |
| GET | `/v3/api-docs/**` | Public | OpenAPI docs | springdoc-openapi |

Note: `/admin/update-role` is now ADMIN-only (`@PreAuthorize("hasRole('ADMIN')")`). `permitAll()` rule removed from `SecurityConfig`.

## Backend Package Patterns

| Area | Current Pattern |
|---|---|
| Controllers | `@RestController`, `@RequestMapping`, `@Valid` on request bodies, Swagger `@Operation`/`@ApiResponses` annotations |
| Services | Interface + Impl pattern (`AuthService`/`AuthServiceImpl`, `UserService`/`UserServiceImpl`) |
| Repositories | Spring Data JPA (`UserRepository extends JpaRepository`) |
| Entities | JPA `@Entity`, Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor` |
| DTOs | Separate request/response DTOs; no entity exposure in API responses |
| Validation | `spring-boot-starter-validation`; `@Valid` on controller params; `@NotBlank` etc. on DTO fields |
| Security | Spring Security + JWT (JJWT 0.11.5); stateless sessions; `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter` |
| Exception handling | `GlobalExceptionHandler` (`@RestControllerAdvice`); custom exceptions (`InvalidCredentialsException`, `UserAlreadyExistsException`, `UserNotFoundException`, `ForbiddenAccessException`); `ErrorResponse` DTO |
| Logging | Lombok `@Slf4j` on all controllers and config classes |

## Database Details

Database: MySQL

```text
Host: localhost:3306
Database name: fly_db
Driver: com.mysql.cj.jdbc.Driver
```

ORM: Spring Data JPA + Hibernate (dialect: `MySQLDialect`)

DDL mode: `none` (schema managed manually — no auto-create/update)

H2 in-memory database included for test scope.

Migration tool: None detected (no Flyway or Liquibase in pom.xml).

### Entities / Tables

| Entity | Table | Fields | Main File |
|---|---|---|---|
| `User` | `users` | id (PK), username (unique), password (BCrypt), role, enabled | `entity/User.java` |

## Environment Variables And Config

| Variable / Config | Used By | Purpose | Required |
|---|---|---|---|
| `VITE_API_BASE_URL` | Frontend | Backend API base URL (default: `http://localhost:8080`) | No (has default) |
| `spring.datasource.url` | Backend | MySQL JDBC URL | Yes |
| `spring.datasource.username` | Backend | MySQL username | Yes |
| `spring.datasource.password` | Backend | MySQL password | Yes |
| `spring.jwt.secret` | Backend | JWT signing secret | Yes |
| `spring.jwt.expiration` | Backend | JWT expiry in ms (default: 86400000 = 24h) | No |

Do not store secret values here. See `application.yaml` for config keys.

## Frontend To Backend Integration

| Frontend Area | Backend Endpoint | Notes |
|---|---|---|
| `LoginPage` via `authService.login()` | `POST /api/auth/login` | Login succeeds → `setAuth(token)` stores token in localStorage and fires background fetch of `getUserDetails` → navigates to `/dashboard` immediately. `userDetails` populates in context once background fetch returns. On error: shows `ErrorResponseDto.message` inline and as toast. URL param `?error=` renders on load (used by 401 redirect). |
| `AuthContext` init (page refresh) | `POST /api/users/userDetails` | On app load, if token valid, decode JWT `sub` → call `getUserDetails(sub)` in background → set `userDetails` in state. `isAuthenticated` is token-based (synchronous), so `ProtectedRoute` passes immediately; userDetails fills in once fetch completes. |
| `api.ts` interceptor (401 handler) | Any protected endpoint | On 401 (excluding login), removes `token` from localStorage and redirects to `/login?error=...` |
| `DashboardPage` sidebar user button | No API call | Opens modal showing `userDetails` from context: `id`, `username`, `role`, `enabled`. No re-fetch on open. |

CORS: Backend allows `http://localhost:5173` on all paths (`/**`).

## Project-Wide Development Rules

### API Security Rules

- All new backend APIs must be protected by default unless explicitly documented as public.
- Public APIs must be listed in `project_details.md` with the reason they are public.
- Valid public APIs may include login, register, health check, forgot password, Swagger/OpenAPI docs, static/public resources, or APIs intentionally exposed without authentication.
- Any API that reads, creates, updates, deletes, or exposes user/application data must require authentication.
- Any API that modifies data must also check authorization/ownership where applicable.
- Do not add a new unprotected API without documenting why it is public.
- Development-only APIs must be clearly marked and must be secured or removed before production.

### Swagger / OpenAPI Rules

- All backend APIs must be documented and accessible from Swagger/OpenAPI.
- Swagger must show every important API endpoint, including protected APIs.
- Protected APIs must remain protected in Swagger. Do not make an API public only to make it work in Swagger.
- Swagger/OpenAPI must support authentication, such as JWT Bearer token, if the backend uses token-based security.
- Every new API should include clear Swagger/OpenAPI details:
  - HTTP method
  - endpoint path
  - auth requirement
  - request body
  - response body
  - validation rules
  - possible error responses
- If a new backend API is added, verify it appears in Swagger.
- If an API is intentionally hidden from Swagger, document the reason in `project_details.md`.
- Swagger should be treated as the backend API contract reference for frontend/backend alignment.
- Frontend request and response handling must match the Swagger/OpenAPI contract.

### Frontend And Backend Contract Rules

- Frontend request payloads must match backend DTO/request models exactly.
- Frontend response handling must match backend response models exactly.
- If a backend API request or response changes, update the frontend usage in the same task.
- If frontend API usage changes, verify the backend endpoint contract in the same task.
- Do not change API paths, request fields, response fields, status codes, auth behavior, or error format without checking both frontend and backend.
- Document every important API contract in `project_details.md`.
- Every new frontend API integration must document:
  - frontend service/function file
  - backend endpoint
  - request body shape
  - response body shape
  - auth requirement
  - loading/error handling pattern

### API Documentation Rules

For each important backend API, document:

- HTTP method
- endpoint path
- auth requirement
- request body shape
- response body shape
- frontend files using it
- backend controller/service files
- known validation rules
- known error cases
- Swagger/OpenAPI availability

### Environment And Config Rules

- Do not hardcode backend URLs in frontend code.
- Do not hardcode secrets in frontend or backend code.
- Use environment variables or config files for URLs, database settings, tokens, secrets, and deployment-specific values.
- Document variable names in `project_details.md`, but never document secret values.
- If a new environment variable is added, update `.env.example` where applicable.

### Database Change Rules

- Do not change database schema casually.
- Any new entity/table/column must be documented in `project_details.md`.
- If schema is manual, document the required SQL or setup step.
- If schema is Hibernate-driven, document the relevant configuration.
- If migrations are added later, document the migration command and location.
- Database changes must be checked against backend DTOs, services, repositories, and frontend expectations where applicable.

### Project Memory Update Rules

Update `project_details.md` whenever any of these change:

- frontend route
- backend API
- request/response contract
- authentication or authorization behavior
- Swagger/OpenAPI contract
- database entity/table/schema
- environment variable
- command
- dependency
- folder structure
- important bug/gotcha
- frontend/backend integration point

Keep updates short, factual, and useful.


## Known Decisions

| Date | Decision | Reason |
|---|---|---|
| 2026-06-01 | Backend sub-project is under `Backend-Java/crypto/` not `Backend-Java/` directly | IntelliJ multi-module setup; pom.xml lives in `crypto/` |
| 2026-06-01 | Only JWT token stored in localStorage; userDetails in React state only | Avoids stale role/username in storage; details always re-fetched from API |
| 2026-06-01 | UserController moved from `/users` to `/api/users` | Needed to fall inside CORS `/api/**` mapping so frontend can call it |
| 2026-06-01 | No test script in frontend package.json | Not configured yet |
| 2026-06-01 | CORS covers all paths `/**` for `localhost:5173` | Expanded from `/api/**`; `.cors(Customizer.withDefaults())` added to `SecurityFilterChain` so OPTIONS preflights pass before auth checks |
| 2026-06-01 | No production `.env` overrides | Not needed as of now |
| 2026-06-01 | Dashboard pages API integration not yet developed | Planned future work |

## Known Issues

| Issue | Area | Status | Notes |
|---|---|---|---|
| `/admin/update-role` is public | Backend | Resolved | Secured with `@PreAuthorize("hasRole('ADMIN')")` and `permitAll()` removed from SecurityConfig on 2026-06-01 |
| No database migration tool | Backend | Open | DDL is `none`; schema exists in MySQL (`fly_db`) but is managed manually; no Flyway/Liquibase |
| No frontend test script | Frontend | Open | `npm run test` does not exist in package.json |
| Dashboard pages have no API calls yet | Frontend | In progress | `OverviewPage`, `JobsDetailsPage`, `TradesPage`, `HistoryPage`, `AnalyticsPage` are yet to be developed |

## Testing Checklist

Before final response, check:

- Frontend builds successfully if frontend changed.
- Backend builds successfully if backend changed.
- No obvious TypeScript or Java compilation errors.
- No broken imports.
- No route conflicts.
- No API response mismatch between frontend and backend.
- No missing environment variables.
- Loading and error states exist where needed.
- `project_details.md` updated if structure, API, database, env, commands, or decisions changed.

## Last Project Indexing Status

Status:

```text
Indexed — 2026-06-01
Shallow inspection completed. All sections populated with actual values from source files.
Updated 2026-06-01: LoginPage form, ToastContext, CORS intentionality, DB existence, dashboard dev status, and production env status confirmed by user.
Updated 2026-06-01: Project-wide API security, Swagger/OpenAPI, frontend/backend contract, config, database, and memory update rules added.
Updated 2026-06-01: Auth flow updated — only token in localStorage; userDetails from API; UserController path moved to /api/users.
Updated 2026-06-01: CORS expanded to /**; login flow changed to navigate-first then background-fetch userDetails.
Updated 2026-06-01: Added .cors(Customizer.withDefaults()) to SecurityFilterChain to fix OPTIONS preflight; isAuthenticated made proper React state to fix login redirect.
```
