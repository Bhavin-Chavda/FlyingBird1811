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
              SchedulerProperties.java      scheduler.* (timezone + per-job cron)
              MarketDataProperties.java     market.* (Delta URL, symbol, buffer, EMA, retry)
              NotificationProperties.java   notification.email.*
            controller/
              AuthController.java
              UserController.java
              AdminUtilityController.java
              SchedulerStatusController.java  scheduler status + history APIs (thin)
              MarketDataController.java       per-job market read APIs (thin dispatch)
            dto/                              (auth/user DTOs only — scheduler DTOs live in scheduler/common)
            entity/  User.java
            exception/  ... JobNotFoundException.java
            marketdata/
              client/DeltaCandleClient.java   Delta /v2/history/candles (generic: resolution+bucket)
              model/  Candle.java, OrderRequest.java
            repository/  UserRepository.java
            scheduler/                        ONE PACKAGE PER JOB — no base class, no shared runner
              common/                         only shared = stateless calc + status
                CandleCalculationUtils.java   stateless EMA + crossover + append + order
                SchedulerTimeUtils.java       stateless IST time helper
                SchedulerConstants.java
                JobStatusService.java / JobStatusServiceImpl.java   RW-locked LIVE status
                JobStatusDto.java, JobExecutionDto.java, CrossoverStateDto.java
              oneMinuteCandle/                fb_1m_job  / thread scheduler-1m-candle
                OneMinuteCandleScheduler.java       own ThreadPoolTaskScheduler + cron + run() + startup seed
                OneMinuteCandleService.java / ...ServiceImpl.java   (seed/run/refill + reads)
                OneMinuteCandleStore.java           own deque + ReentrantReadWriteLock + signal state
                OneMinuteCandleTasklet.java         own Spring Batch step body
                OneMinuteCandleBatchConfig.java     own Job+Step (job name oneMinuteCandleJob)
              fiveMinuteCandle/               fb_5m_job  / thread scheduler-5m-candle (same 6 classes)
              fifteenMinuteCandle/            fb_15m_job / thread scheduler-15m-candle (same 6 classes)
            service/
              AuthService.java / AuthServiceImpl.java
              UserService.java / UserServiceImpl.java
              MailService.java / MailServiceImpl.java   (shared infra: SMTP)
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
| GET | `/api/scheduler/jobs` | JWT required | List all scheduler (cron) job statuses | `SchedulerStatusController` |
| GET | `/api/scheduler/jobs/{jobId}` | JWT required | Get one scheduler job status (404 if unknown) | `SchedulerStatusController` |
| GET | `/api/scheduler/history?limit=N` | JWT required | Persisted batch job-execution history (BATCH_* tables) | `SchedulerStatusController` |
| GET | `/api/market/{timeframe}/crossover-state` | JWT required | Latest EMA crossover signal state (1m/5m/15m) | `MarketDataController` |
| GET | `/api/market/{timeframe}/last-candle` | JWT required | Most recent candle in buffer (1m/5m/15m) | `MarketDataController` |
| GET | `/api/market/{timeframe}/buffer` | JWT required | Full candle buffer snapshot (1m/5m/15m) | `MarketDataController` |
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

## Schedulers / Background Jobs

Migrated from a Python/FastAPI + APScheduler backend (`D:\Bhavin\Python-FastAPI-Source`).

- **Fully per-job (2026-06-01):** one self-contained package per job — `scheduler/oneMinuteCandle`, `scheduler/fiveMinuteCandle`, `scheduler/fifteenMinuteCandle` — each with its own `*Scheduler`, `*Service`/`*ServiceImpl`, and `*Store`. **No abstract base class, no shared launcher, no common run method, no central scheduler** (removed `AbstractCandleScheduler`, `CandleJobLauncher`, `MarketDataJobService`, `CandleBufferService`, `CandleStore`/`CandleStoreRegistry`, `MarketDataScheduler`).
- **Each job owns its own dedicated single-thread `ThreadPoolTaskScheduler`** created in its own scheduler class, named per job: `scheduler-1m-candle`, `scheduler-5m-candle`, `scheduler-15m-candle`. Each scheduler registers its own `CronTrigger(cron, ZoneId)` in `@PostConstruct` and has its OWN `run()` (overlap guard + status + logging — duplicated per job by design, not shared). A slow 15m run blocks only its own next tick — never the 5m/1m jobs (e.g. at 13:15 the 5m and 15m jobs run concurrently on separate threads).
- Jobs (cron in IST, from `application.yaml` `scheduler.cron.*`):

| Job ID | Schedule (cron, IST) | Package / thread |
|---|---|---|
| `fb_1m_job` | `5 * * * * *` | `oneMinuteCandle` / `scheduler-1m-candle` |
| `fb_5m_job` | `5 */5 * * * *` | `fiveMinuteCandle` / `scheduler-5m-candle` |
| `fb_15m_job` | `15 */15 * * * *` | `fifteenMinuteCandle` / `scheduler-15m-candle` |

- **Only shared scheduler code = stateless calculation** (`scheduler/common/CandleCalculationUtils`: EMA seed/next, append-with-EMA on a caller-locked buffer, crossover detection, order build; `SchedulerTimeUtils`) **+ shared status** (`JobStatusService`). Infra clients `DeltaCandleClient` (generic: resolution+bucket) and `MailService` are shared, thread-safe, stateless.
- **Per-job data store:** each `*Store` encapsulates its bounded `Deque<Candle>` + crossover signal state behind its OWN `ReentrantReadWriteLock`. Writes (seed/append/record) take the write lock; reads (`snapshot`/`size`/`lastCandle`/`crossoverSnapshot`) take the read lock and return **immutable copies/DTOs** — the internal deque is never exposed. The Delta HTTP fetch happens in the service OUTSIDE the lock; only the in-memory mutation is locked.
- **Live job status** is in-memory in `JobStatusService` (RW-locked; consistent snapshots). Fields: jobId, jobName, cron, threadName, running, lastStartTime, lastEndTime, lastSuccessTime, lastFailureTime, nextRunTime, totalRuns, totalFailures, lastDurationMs, lastDataCount, lastErrorMessage.
- **Durable run history (per-job Spring Batch, common table):** each job owns its OWN batch `Job`+`Step`+`Tasklet` (`<job>BatchConfig` + `<job>Tasklet` in its package) — there is **no shared batch job/tasklet**. Each scheduler launches **its own** job via `JobOperator.start` on its own thread, so every run is recorded in the **common** Spring Batch tables (`BATCH_JOB_EXECUTION`, etc.) under job names `oneMinuteCandleJob`/`fiveMinuteCandleJob`/`fifteenMinuteCandleJob`. `GET /api/scheduler/history` reads those tables via `JobRepository` (newest first). BATCH_* schema auto-created on startup via `spring.sql.init` (bundled `schema-mysql.sql`, `continue-on-error`).
- **Startup seeding:** each job seeds its own 300-candle buffer at startup on its OWN thread (`scheduler.seed-on-startup=true`, off in tests). **Refill-on-error:** if fetching the next candle throws, the job empties + refills the buffer via the initial seed logic; if the refill fetch also fails, the previous buffer is preserved and the failure is recorded.
- **API reads on the main/request thread:** the read APIs (`/api/scheduler/**`, `/api/market/**`) run on the Tomcat request thread and read via the stores' read-lock snapshots / `JobStatusService` / `JobRepository` — always consistent, never exposing internal collections.
- DI uses `@RequiredArgsConstructor` (final fields); the per-job `Job` bean is resolved by constructor parameter name (matches the bean name) — no shared launcher/qualifier.

### Per-job run flow (each job, on its own thread)

`*Scheduler.run()` → `tryStart` overlap guard → `*ServiceImpl.run()`: self-seed if empty (fetch OUTSIDE lock) → fetch latest candle (OUTSIDE lock) → `*Store.appendLatest` (locked append-with-EMA, dedup by time) → `CandleCalculationUtils.evaluateSignal` → `*Store.recordEvaluation` (atomic; reports a NEUTRAL→signal transition) → on transition build a bracket order + email via `MailService` (OUTSIDE lock) → scheduler records success/failure + duration + record count.

- **Candle source:** `DeltaCandleClient` → Delta `GET /v2/history/candles` (public, India base URL, start/end in seconds, field `time`). Retries up to `market.retry-attempts` (default 3) with linear backoff.
- **EMA:** TradingView-style (SMA seed + `k=2/(period+1)`), periods 21/30/35/40/45/50/60/200 — in `CandleCalculationUtils` (stateless).
- **Buffer:** per-job `*Store` — bounded deque (length 300); seed fetches `bufferLength + 200*seedMultiple` then keeps the last `bufferLength`. Each job self-seeds on its first run (no central startup seeder).
- **Email:** `MailServiceImpl` (`JavaMailSender`) — enabled by default; no-op if SMTP/recipients unset; never breaks a job on failure.
- **Thread visibility:** e.g. `[scheduler-5m-candle] Started 5-minute candle fetch` / `Completed 5-minute candle fetch in 420ms, records=300` / `Failed 5-minute candle fetch: <error>`.
- Market-data read APIs: `/api/market/{tf}/crossover-state`, `/last-candle`, `/buffer` (tf = 1m/5m/15m), dispatched by a thin `MarketDataController` to the owning job service.
- **Spring Batch (per-job) for durable history:** re-added as `spring-boot-starter-batch`. Each job has its OWN `Job`/`Step`/`Tasklet` (no shared batch job/runner); all executions land in the **one common** `BATCH_*` table set. This keeps durable history while preserving "no shared run."

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

**Spring Batch tables (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_JOB_EXECUTION_PARAMS`, `BATCH_JOB_EXECUTION_CONTEXT`, `BATCH_STEP_EXECUTION`, `BATCH_STEP_EXECUTION_CONTEXT` + `*_SEQ`):** created in `fly_db` on startup (9 tables, verified present + idempotent across restarts). They store the scheduler job-execution history. Not JPA entities — managed by Spring Batch.

> Boot 4 **removed** the old `spring.batch.jdbc.initialize-schema` auto-init. Schema is now created via `spring.sql.init` pointing at Spring Batch's bundled `classpath:org/springframework/batch/core/schema-mysql.sql`, with `spring.sql.init.continue-on-error=true` so restarts are no-ops once the tables exist. Tests (H2) set `spring.sql.init.mode=never` (jobs don't run in tests).

## Environment Variables And Config

| Variable / Config | Used By | Purpose | Required |
|---|---|---|---|
| `VITE_API_BASE_URL` | Frontend | Backend API base URL (default: `http://localhost:8080`) | No (has default) |
| `spring.datasource.url` | Backend | MySQL JDBC URL | Yes |
| `spring.datasource.username` | Backend | MySQL username | Yes |
| `spring.datasource.password` | Backend | MySQL password | Yes |
| `spring.jwt.secret` | Backend | JWT signing secret | Yes |
| `spring.jwt.expiration` | Backend | JWT expiry in ms (default: 86400000 = 24h) | No |
| `DELTA_BASE_URL` | Backend | Delta Exchange REST base URL (default `https://api.india.delta.exchange`) | No |
| `CANDLE_SYMBOL` | Backend | Candle instrument symbol (default `BTCUSD`) | No |
| `DEMO_BTCUSD_ID` | Backend | Delta product id for bracket orders in signal email | No |
| `EMAIL_ENABLED` | Backend | Enable crossover signal emails (default `true`) | No |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USER` / `SMTP_PASS` / `SMTP_STARTTLS` | Backend | SMTP server config (`spring.mail.*`) | Only if `EMAIL_ENABLED=true` |
| `EMAIL_FROM` / `EMAIL_TO` / `EMAIL_CC` / `EMAIL_BCC` | Backend | Signal email sender/recipients | Only if `EMAIL_ENABLED=true` |

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
| 2026-06-01 | Python/FastAPI scheduler migrated as 3 jobs (1m/5m/15m), not 4 | Source has exactly 3 APScheduler jobs |
| 2026-06-01 | Job status kept in-memory (no DB table) | Mirrors Python; no schema change approved this phase |
| 2026-06-01 | Scheduler infra migrated first; trading/email logic deferred | Phase 1 = thread pool + status tracking + status APIs only |
| 2026-06-01 | Phase 2: migrated Delta fetch + EMA + crossover + email business logic | Completes the Python→Java scheduler migration |
| 2026-06-01 | 3 duplicated Python modules collapsed into timeframe-parameterized services | Avoids migrating duplicate logic; single source per concern |
| 2026-06-01 | Cron moved to application.yaml (`scheduler.cron.*`) | Single source of truth; removed duplicate cron literals in @Scheduled + enum |
| 2026-06-01 | Added `spring-boot-starter-mail` dependency | Required for SMTP signal emails (no JDK SMTP); approved under "add deps as required" |
| 2026-06-01 | Signal email disabled by default; live order placement not migrated | Safe defaults; app boots without SMTP; order placement out of scope |
| 2026-06-01 | Spring Batch added for persistent job history (BATCH_* tables) | User requested durable history; tables auto-created in fly_db (`initialize-schema=always`, verified idempotent) |
| 2026-06-01 | Each cron tick launches batch `marketDataJob` (timeframe param) | Records every run in BATCH_JOB_EXECUTION; in-memory status kept for live APIs + overlap guard |
| 2026-06-01 | Delta fetch retries 3× with backoff before failing | User requested retry on improper Delta responses |
| 2026-06-01 | Signal email now ENABLED by default (`EMAIL_ENABLED:true`) | User wants notifications on every start; no-op if SMTP/recipients unset |
| 2026-06-01 | `spring.batch.job.enabled=false`; tasklet is null-safe on missing timeframe | Prevent batch auto-run at startup; harmless if property ignored |
| 2026-06-01 | Boot 4 removed batch auto schema-init; use `spring.sql.init` + bundled `schema-mysql.sql` (`continue-on-error=true`) | `spring.batch.jdbc.initialize-schema` no longer exists in Boot 4 — was silently ignored, so no BATCH_* tables were created |
| 2026-06-01 | Use non-deprecated `JobOperator` (launch) + `JobRepository` (history); not `JobLauncher`/`JobExplorer` | Those are deprecated-for-removal in Spring Batch 6.0 |
| 2026-06-01 | Tests use H2; `market.seed-on-startup=false` + once-a-year cron + `spring.sql.init.mode=never` | Hermetic, fast `contextLoads` (no Delta calls, no MySQL needed for the build) |
| 2026-06-01 | Delta candle `start`/`end` kept in SECONDS, field `time` (per working Python contract) | Matches the proven implementation; an auto-summary of the docs claimed microseconds/`timestamp` — treat as unverified |
| 2026-06-01 | Scheduler refactored: per-job classes + per-job dedicated threads + per-job CandleStore | Removed monolithic MarketDataScheduler + shared deque/lock map; readability, maintainability, true per-job thread isolation |
| 2026-06-01 | Dropped `@Scheduled` in favour of manual `CronTrigger` on a per-job single-thread scheduler | Needed for dedicated, named, isolated threads per job (`scheduler-{1m,5m,15m}-candle`) |
| 2026-06-01 | `JobStatus`/`JobStatusService` moved to `scheduler/common`; `JobStatusResponseDto` gained threadName/lastEndTime/lastDurationMs/lastDataCount | Cleaner scheduler package; richer status (additive DTO change — existing fields preserved) |
| 2026-06-01 | Full per-job scheduler refactor: removed `AbstractCandleScheduler`, `CandleJobLauncher`, `MarketDataJobService`, `CandleBufferService`, `CandleStore`/registry | User required no base class, no shared run/launcher; each job owns its own scheduler/service/store/thread |
| 2026-06-01 | Removed Spring Batch from scheduler (dependency + `marketDataJob`/tasklet + `spring.sql.init` batch schema) | The single parameterized batch job was a shared run flow (forbidden); durable history needs either that or a new table (schema change forbidden) → in-memory history instead. BATCH_* tables left intact but unused. |
| 2026-06-01 | Only shared scheduler code = `CandleCalculationUtils` (stateless), `SchedulerTimeUtils`, `JobStatusService` | Per the per-job design: share calculation + status only |
| 2026-06-01 | One shared `CrossoverStateDto` read DTO (not 3 identical per-job DTOs) | Practical; avoids unnecessary duplication + parameterized-controller typing issues |
| 2026-06-01 | Re-added Spring Batch as PER-JOB jobs (own Job/Step/Tasklet/BatchConfig per package); history in the common BATCH_* tables | User wants durable job history in one table while keeping "no shared run" — each job launches its own batch job via JobOperator |
| 2026-06-01 | Each job seeds its own 300-candle buffer at startup on its own thread; refill-on-fetch-error | Implements the Python core idea + the requested error-recovery (empty + refill) |
| 2026-06-01 | `@RequiredArgsConstructor` for DI; per-job `Job` resolved by constructor param name | No explicit constructors; no shared launcher/qualifier needed |

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
Updated 2026-06-01: Migrated Python/FastAPI scheduler infrastructure (phase 1) — SchedulerConfig, MarketDataScheduler (1m/5m/15m), JobStatusService (in-memory, RW-locked), GET /api/scheduler/jobs[/{jobId}] (JWT-protected, Swagger-documented). Trading/email business logic deferred.
Updated 2026-06-01: Phase 2 — migrated Delta candle fetch (DeltaCandleClient), EMA (EmaService), buffer (CandleBufferService), crossover signal (CrossoverService), email (MailService), startup seeding (MarketDataInitializer), market-data read APIs (/api/market/**). Cron centralised in application.yaml; added spring-boot-starter-mail. Backend compiles clean.
Updated 2026-06-01: Phase 3 — added Spring Batch (spring-boot-starter-batch) for persistent job history; cron ticks launch batch marketDataJob via JobOperator. Added GET /api/scheduler/history (JobRepository). Delta fetch retries 3× with backoff. Email enabled by default. Thread name logged in job runs.
Updated 2026-06-01: Phase 3 fixes — (1) failing contextLoads test fixed: src/test/resources/application.yaml was missing scheduler.cron.* (added + defaults on @Scheduled placeholders; tests now H2-only, hermetic, BUILD SUCCESS). (2) Migrated off deprecated JobLauncher/JobExplorer to JobOperator/JobRepository. (3) Boot 4 removed spring.batch.jdbc.initialize-schema (silently ignored → no tables); switched to spring.sql.init + bundled schema-mysql.sql (continue-on-error). VERIFIED via JDBC: 9 BATCH_* tables created in fly_db, restart-idempotent. Delta contract (/v2/history/candles, India base URL, no auth, seconds, field 'time') re-checked against docs.delta.exchange.
Updated 2026-06-01: Phase 4 — scheduler module refactor. Removed monolithic MarketDataScheduler + shared EnumMap deque/lock store. Added per-job classes (scheduler/candle/{One,Five,Fifteen}MinuteCandleScheduler extends AbstractCandleScheduler), each on its OWN dedicated single-thread scheduler (scheduler-1m/5m/15m-candle) via CronTrigger (no @Scheduled). Per-timeframe CandleStore (deque + ReentrantReadWriteLock, immutable-copy reads) via CandleStoreRegistry; CandleBufferServiceImpl is now a thin orchestrator. CandleJobLauncher centralises overlap-guard + batch launch + status. JobStatus/JobStatusService moved to scheduler/common; JobStatusResponseDto gained threadName/lastEndTime/lastDurationMs/lastDataCount (additive). APIs/paths unchanged. VERIFIED: clean compile, BUILD SUCCESS (tests), and 3 dedicated named schedulers initialise at startup. See Backend-Java/SCHEDULER_README.md.
Updated 2026-06-01: Phase 6 — per-job Spring Batch re-added for DURABLE history in the common BATCH_* tables (each job owns its own Job/Step/Tasklet/BatchConfig; no shared batch job; launched via JobOperator by each scheduler). Each job seeds its own 300-candle buffer at startup on its own thread; on next-candle fetch error the buffer is emptied + refilled (initial logic). DI switched to @RequiredArgsConstructor (per-job Job resolved by param name). API reads run on the request thread, thread-safe. VERIFIED: clean compile, BUILD SUCCESS (tests, 7s), 3 schedulers each seeded 300 candles on their own thread at startup, 9 BATCH_* tables present in fly_db.
Updated 2026-06-01: Phase 5 — FULL per-job refactor. Removed AbstractCandleScheduler, CandleJobLauncher (shared launcher), MarketDataJobService (shared run), CandleBufferService + CandleStore/registry (shared store), EmaService/CrossoverService (folded into stateless CandleCalculationUtils), MarketDataInitializer, and Spring Batch (dependency + BatchConfig + tasklet + spring.sql.init + JobExecution/JobStatus batch DTOs). New layout: scheduler/{oneMinuteCandle,fiveMinuteCandle,fifteenMinuteCandle} each with own Scheduler+Service+ServiceImpl+Store (own ThreadPoolTaskScheduler + ReentrantReadWriteLock); scheduler/common = CandleCalculationUtils + SchedulerTimeUtils + JobStatusService(+Impl) + DTOs (JobStatusDto, JobExecutionDto, CrossoverStateDto). DeltaCandleClient now generic (resolution+bucket). /api/scheduler/history backed by in-memory per-job history. APIs/paths/auth unchanged. VERIFIED: clean compile, BUILD SUCCESS (tests, 7.5s), 3 independent named schedulers init at startup. SCHEDULER_README.md updated.
```
