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
        jobs.ts                JOB_CONFIGS (4 jobs: id/timeframe/label — 1m/5m/15m/1h)
      context/
        AuthContext.tsx
        ToastContext.tsx
      pages/
        LoginPage.tsx
        DashboardPage.tsx
        dashboard/
          OverviewPage.tsx
          JobsDetailsPage.tsx
          PaperTradingPage.tsx   paper-trades table + filters + detail modal
          TradesPage.tsx
          HistoryPage.tsx
          AnalyticsPage.tsx
      services/
        api.ts                 axios instance with JWT interceptor
        authService.ts         login / logout / isTokenValid
        jobService.ts          getJobDetails(timeframe) → /api/jobs/{tf}/details
        paperTradeService.ts   getPaperTrades() → /api/paper-trades
        adminService.ts        registerUserByAdmin() → POST /api/admin/users/register; disableUserByAdmin() → POST /api/admin/users/disable
      types/
        auth.ts                LoginRequestDto, AuthResponseDto, User, ErrorResponseDto
        jobDetails.ts          JobDetailsResponseDto, JobStatusDto, CrossoverStateDto, Candle
        paperTrade.ts          PaperTrade, PaperCandle (mirrors PaperTradeDetailsResponseDto)
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
              BatchInfrastructureConfig.java  @EnableBatchProcessing + @EnableJdbcJobRepository (JDBC JobRepository → BATCH_* tables)
              SchedulerProperties.java      scheduler.* (timezone + per-job cron + job-details.candle-count)
              MarketDataProperties.java     market.* (Delta URL, symbol, buffer, EMA, retry)
              NotificationProperties.java   notification.email.*
            controller/
              AuthController.java
              UserController.java
              AdminUtilityController.java
              AdminUserController.java          ADMIN-only POST /api/admin/users/register
              SchedulerStatusController.java  scheduler status + history APIs (thin)
              MarketDataController.java       per-job market read APIs (thin dispatch)
              JobDetailsController.java       aggregate job-details API (thin) /api/jobs/{timeframe}/details
            dto/                              (auth/user DTOs only — scheduler DTOs live in scheduler/common)
            entity/  User.java
            exception/  ... JobNotFoundException.java
            marketdata/
              client/DeltaCandleClient.java   Delta /v2/history/candles (generic: resolution+bucket)
              model/  Candle.java, OrderRequest.java
            papertrading/                     Paper Trading feature (pattern detection → paper trades)
              config/  PaperTradingProperties.java, PaperTradingAsyncConfig.java (@EnableAsync)
              controller/  PaperTradeController.java   GET /api/paper-trades
              dto/  PatternDetectionResultDto, PaperTradeDetailsResponseDto, PaperCandleResponseDto, CrossoverStateSnapshotDto
              entity/  PaperCandle.java, PaperTrade.java
              enums/  ChartPatternName, TradeDirection, PaperTradeStatus, CloseReason
              event/  PatternDetectionRequestedEvent, PatternDetectionEventListener (@Async)
              pattern/  PatternDetector.java / PatternDetectorImpl.java (12 Phase-1 detectors, pure)
              repository/  PaperCandleRepository.java, PaperTradeRepository.java
              service/  PaperTradeCreationService, TradeEvaluator, PatternDetection{Processor,Orchestrator}, TradeEvaluationOrchestrator, PaperTradingOrchestrator, PaperTradeQueryService, CrossoverStateSnapshotService (+ impl/)
              (DTC paper-trade logic is inlined in each scheduler *CandleServiceImpl — no shared service)
              util/  SwingPointUtils, PatternMathUtils, AtrUtils, RiskRewardUtils, CandleTimeUtils
            repository/  UserRepository.java
            scheduler/                        ONE PACKAGE PER JOB — no base class, no shared runner
              common/                         only shared = stateless calc + status
                CandleCalculationUtils.java   stateless EMA + crossover + append + order
                SchedulerTimeUtils.java       stateless IST time helper
                SchedulerConstants.java
                JobStatusService.java / JobStatusServiceImpl.java   RW-locked LIVE status
                JobDetailsService.java / JobDetailsServiceImpl.java aggregate job details (status+crossover+last N candles, N configurable)
                JobStatusDto.java, JobExecutionDto.java, CrossoverStateDto.java, JobDetailsResponseDto.java
                Timeframe.java, JobId.java, StringToTimeframeConverter.java, StringToJobIdConverter.java
              oneMinuteCandle/                fb_1m_job  / thread scheduler-1m-candle
                OneMinuteCandleScheduler.java       own ThreadPoolTaskScheduler + cron + run() + startup seed
                OneMinuteCandleService.java / ...ServiceImpl.java   (seed/run/refill + reads)
                OneMinuteCandleStore.java           own deque + ReentrantReadWriteLock + signal state
                OneMinuteCandleTasklet.java         own Spring Batch step body
                OneMinuteCandleBatchConfig.java     own Job+Step (job name oneMinuteCandleJob)
              fiveMinuteCandle/               fb_5m_job  / thread scheduler-5m-candle (same 6 classes)
              fifteenMinuteCandle/            fb_15m_job / thread scheduler-15m-candle (same 6 classes)
              hourlyCandle/                   fb_1h_job  / thread scheduler-1h-candle (same 6 classes; resolution 1h, bucket 3600s)
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
| `/dashboard/paper-trading` | Paper Trading tab (pattern paper trades) | `pages/dashboard/PaperTradingPage.tsx` |
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
| Dashboard sidebar | `DashboardPage.tsx` renders the shell (`.db-shell` → `.sidebar` + `.db-body`). Sidebar is **drag-resizable** on desktop (handle on right edge, `ew-resize`, width clamped 72–320px in local `sidebarWidth` state — not persisted). Collapse/minimize toggle lives **inside the sidebar nav, below Analytics** (`.sb-collapse-btn`). The desktop top header was removed; `.db-topbar` (hamburger + brand) is **mobile-only** and opens the off-canvas drawer. Theme unchanged. |
| Forms | Controlled inputs via `useState`; client-side required-field validation (non-blank); `noValidate` on form element |
| Auth handling | Only `token` stored in `localStorage`; `isAuthenticated` is React state (initialised from `isTokenValid()`), updated via `setAuth`/`logout`; `userDetails` held in `AuthContext` state, fetched in background after login and on page refresh. **Stale-token hardening (2026-06-04):** the axios request interceptor never sends an expired token — it purges it from `localStorage` instead; on app load `AuthContext` also purges an invalid/expired token; the 401 response interceptor only clears+redirects when the *currently stored* token is itself invalid (so a stale in-flight 401 can't clobber a freshly-established session). Login replaces the token; logout removes it (`markManualLogout` drops in-flight 401s). |
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
| POST | `/api/admin/users/register` | JWT + ADMIN role | Admin creates a user (`{username,password,role}`, role=USER/ADMIN); 409 on duplicate username | `AdminUserController` |
| POST | `/api/admin/users/disable` | JWT + ADMIN role | Admin disables a user (`{username}` → `enabled=false`); 404 if unknown, **403 if target has ADMIN role** (admins can't be disabled) | `AdminUserController` |
| GET | `/api/scheduler/jobs` | JWT required | List all scheduler (cron) job statuses | `SchedulerStatusController` |
| GET | `/api/scheduler/jobs/{jobId}` | JWT required | Get one scheduler job status (404 if unknown) | `SchedulerStatusController` |
| GET | `/api/scheduler/history?limit=N` | JWT required | Persisted batch job-execution history (BATCH_* tables) | `SchedulerStatusController` |
| GET | `/api/market/{timeframe}/crossover-state` | JWT required | Latest EMA crossover signal state (1m/5m/15m/1h) | `MarketDataController` |
| GET | `/api/market/{timeframe}/last-candle` | JWT required | Most recent candle in buffer (1m/5m/15m/1h) | `MarketDataController` |
| GET | `/api/market/{timeframe}/buffer` | JWT required | Full candle buffer snapshot (1m/5m/15m/1h) | `MarketDataController` |
| GET | `/api/jobs/{timeframe}/details` | JWT required | Aggregate job details: status + last crossover + last N candles (N = `scheduler.job-details.candle-count`) (1m/5m/15m/1h) | `JobDetailsController` |
| GET | `/api/paper-trades` | JWT required | All paper trades + related signal candle (newest first); optional filters status/timeframe/tradeType/patternName/safeTrade/fromDate/toDate | `PaperTradeController` |
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

- **Fully per-job (2026-06-01; 4th hourly job added 2026-06-02):** one self-contained package per job — `scheduler/oneMinuteCandle`, `scheduler/fiveMinuteCandle`, `scheduler/fifteenMinuteCandle`, `scheduler/hourlyCandle` — each with its own `*Scheduler`, `*Service`/`*ServiceImpl`, and `*Store`. **No abstract base class, no shared launcher, no common run method, no central scheduler** (removed `AbstractCandleScheduler`, `CandleJobLauncher`, `MarketDataJobService`, `CandleBufferService`, `CandleStore`/`CandleStoreRegistry`, `MarketDataScheduler`).
- **Each job owns its own dedicated single-thread `ThreadPoolTaskScheduler`** created in its own scheduler class, named per job: `scheduler-1m-candle`, `scheduler-5m-candle`, `scheduler-15m-candle`, `scheduler-1h-candle`. Each scheduler registers its own `CronTrigger(cron, ZoneId)` in `@PostConstruct` and has its OWN `run()` (overlap guard + status + logging — duplicated per job by design, not shared). A slow 15m run blocks only its own next tick — never the 5m/1m jobs (e.g. at 13:15 the 5m and 15m jobs run concurrently on separate threads).
- Jobs (cron in IST, from `application.yaml` `scheduler.cron.*`):

| Job ID | Schedule (cron, IST) | Package / thread |
|---|---|---|
| `fb_1m_job` | `5 * * * * *` | `oneMinuteCandle` / `scheduler-1m-candle` |
| `fb_5m_job` | `5 */5 * * * *` | `fiveMinuteCandle` / `scheduler-5m-candle` |
| `fb_15m_job` | `15 */15 * * * *` | `fifteenMinuteCandle` / `scheduler-15m-candle` |
| `fb_1h_job` | `30 0 * * * *` | `hourlyCandle` / `scheduler-1h-candle` (Delta resolution `1h`, bucket 3600s) |

- **Only shared scheduler code = stateless calculation** (`scheduler/common/CandleCalculationUtils`: EMA seed/next, append-with-EMA on a caller-locked buffer, crossover detection, order build; `SchedulerTimeUtils`) **+ shared status** (`JobStatusService`). Infra clients `DeltaCandleClient` (generic: resolution+bucket) and `MailService` are shared, thread-safe, stateless.
- **Per-job data store:** each `*Store` encapsulates its bounded `Deque<Candle>` + crossover signal state behind its OWN `ReentrantReadWriteLock`. Writes (seed/append/record) take the write lock; reads (`snapshot`/`size`/`lastCandle`/`crossoverSnapshot`) take the read lock and return **immutable copies/DTOs** — the internal deque is never exposed. The Delta HTTP fetch happens in the service OUTSIDE the lock; only the in-memory mutation is locked.
- **Live job status** is in-memory in `JobStatusService` (RW-locked; consistent snapshots). Fields: jobId, jobName, cron, threadName, running, lastStartTime, lastEndTime, lastSuccessTime, lastFailureTime, nextRunTime, totalRuns, totalFailures, lastDurationMs, lastDataCount, lastErrorMessage.
- **Durable run history (per-job Spring Batch, common table):** each job owns its OWN batch `Job`+`Step`+`Tasklet` (`<job>BatchConfig` + `<job>Tasklet` in its package) — there is **no shared batch job/tasklet**. Each scheduler launches **its own** job via `JobOperator.start` on its own thread, so every run is recorded in the **common** Spring Batch tables (`BATCH_JOB_EXECUTION`, etc.) under job names `oneMinuteCandleJob`/`fiveMinuteCandleJob`/`fifteenMinuteCandleJob`/`hourlyCandleJob`. `GET /api/scheduler/history` reads those tables via `JobRepository` (newest first). BATCH_* schema auto-created on startup via `spring.sql.init` (bundled `schema-mysql.sql`, `continue-on-error`).
- **Startup seeding:** each job seeds its own 300-candle buffer at startup on its OWN thread (`scheduler.seed-on-startup=true`, off in tests). **Refill-on-error:** if fetching the next candle throws, the job empties + refills the buffer via the initial seed logic; if the refill fetch also fails, the previous buffer is preserved and the failure is recorded.
- **Initial crossover at seed (2026-06-02):** each `*ServiceImpl.seed()` calls `recordInitialCrossover()` after `store.seedReplace(...)` — it evaluates the EMA-stack signal on the freshly seeded buffer and records it (no signal email on seed). So `lastCrossOverState` is populated immediately after startup seeding, not only after the first scheduled `run()`. This matters most for low-frequency jobs (e.g. the hourly job's crossover would otherwise stay null until the top of the hour). Applied identically to all four jobs.
- **API reads on the main/request thread:** the read APIs (`/api/scheduler/**`, `/api/market/**`) run on the Tomcat request thread and read via the stores' read-lock snapshots / `JobStatusService` / `JobRepository` — always consistent, never exposing internal collections.
- DI uses `@RequiredArgsConstructor` (final fields); the per-job `Job` bean is resolved by constructor parameter name (matches the bean name) — no shared launcher/qualifier.

### Per-job run flow (each job, on its own thread)

`*Scheduler.run()` → `tryStart` overlap guard → `*ServiceImpl.run()`: self-seed if empty (fetch OUTSIDE lock) → fetch latest candle (OUTSIDE lock) → `*Store.appendLatest` (locked append-with-EMA, dedup by time) → `CandleCalculationUtils.evaluateSignal` → `*Store.recordEvaluation` (atomic; reports a NEUTRAL→signal transition) → on transition `emitSignal` builds the **DTC trade plan** (`createDtcPaperTrade` → `PatternDetectionResultDto`), persists the paper trade if the DTC flag is on, then emails that plan via `MailService.sendSignalEmail(candle, Object payload, …)` (OUTSIDE lock) → scheduler records success/failure + duration + record count.

- **Candle source:** `DeltaCandleClient` → Delta `GET /v2/history/candles` (public, India base URL, start/end in seconds, field `time`). Retries up to `market.retry-attempts` (default 3) with linear backoff.
- **EMA:** TradingView-style (SMA seed + `k=2/(period+1)`), periods 21/30/35/40/45/50/60/200 — in `CandleCalculationUtils` (stateless).
- **Buffer:** per-job `*Store` — bounded deque (length 300); seed fetches `bufferLength + 200*seedMultiple` then keeps the last `bufferLength`. Each job self-seeds on its first run (no central startup seeder).
- **Email:** `MailServiceImpl` (`JavaMailSender`) — enabled by default; no-op if SMTP/recipients unset; never breaks a job on failure.
- **Per-timeframe signal-email toggle (2026-06-05):** `notification.email.signal.{one-minute,five-minute,fifteen-minute,one-hour}` (all default `true`, env `SIGNAL_EMAIL_{1M,5M,15M,1H}`), bound in `NotificationProperties.Signal` with `isEmailEnabledFor(resolution)`. Each `*ServiceImpl.emitSignal()` runs its full signal calculation (order build etc.) and checks `notificationProps.isEmailEnabledFor(RESOLUTION)` **only immediately before the `MailService.sendSignalEmail(...)` call** (gate on mail send only, so future `emitSignal` work still runs when email is off); when `false` it logs `"Signal email disabled for timeframe {} ..."` and returns without sending — the crossover is still **detected and recorded** (`recordEvaluation`), the scheduler still completes normally (not a failure). The global `notification.email.enabled` master switch (checked in `MailServiceImpl`) is unchanged and still applies. Unknown/null resolution fails open (`true`).
- **Thread visibility:** e.g. `[scheduler-5m-candle] Started 5-minute candle fetch` / `Completed 5-minute candle fetch in 420ms, records=300` / `Failed 5-minute candle fetch: <error>`.
- Market-data read APIs: `/api/market/{tf}/crossover-state`, `/last-candle`, `/buffer` (tf = 1m/5m/15m/1h), dispatched by a thin `MarketDataController` to the owning job service.
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
| `PaperCandle` | `paper_candles` | candle_id (PK), timeframe, candle_time, OHLCV (DECIMAL), created/updated_at; unique (timeframe, candle_time) | `papertrading/entity/PaperCandle.java` |
| `PaperTrade` | `paper_trades` | trade_id (PK), timeframe, trade_type, trade_status, pattern_name, candle_time, **candle_id (FK→paper_candles, NOT NULL, `@ManyToOne(optional=false)`)**, trade_price/stop_loss/initial_stop_loss/tp1-4 (DECIMAL), safe_trade + tp1-4_achieved (bool), close_price/close_reason, opened/closed/last_evaluated_at, confidence_score, detection_reason, breakout_level/risk_amount/atr_at_detection, **one/five/fifteen-minute + one-hour `_crossover_state` (VARCHAR(16), snapshot at creation)**, version (@Version); unique (timeframe, pattern_name, trade_type, candle_time) | `papertrading/entity/PaperTrade.java` |

**`paper_candles` + `paper_trades`** are created on startup by `spring.sql.init` (`classpath:db/paper-trading-schema.sql`, `CREATE TABLE IF NOT EXISTS`, appended to `schema-locations` next to the batch schema). **Hibernate `ddl-auto` stays `none`** — the SQL file owns the schema (non-destructive, idempotent). Tests (H2, `spring.sql.init.mode=never`) don't create them; the paper-trading repositories aren't queried at startup so the context still loads.

> **Recreation after a manual drop (2026-06-06):** since the user dropped the paper-trading tables, they are **recreated on the next startup by `spring.sql.init`** (not by Hibernate — `ddl-auto` is unchanged at `none`). The schema SQL was updated in the same commit: `paper_trades.candle_id` is now `BIGINT NOT NULL` (every trade must reference its signal candle; entity uses `@ManyToOne(optional=false)` + `@JoinColumn(nullable=false)`), and four new `*_crossover_state VARCHAR(16)` columns were added to the `CREATE TABLE`. A fresh DB picks these up automatically. (For a DB that was *not* dropped, the new columns/NOT-NULL would need a manual `ALTER` — there's no idempotent `ADD COLUMN` in MySQL 8, so it's intentionally left to the drop-and-recreate path the user already took.)

**Spring Batch tables (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_JOB_EXECUTION_PARAMS`, `BATCH_JOB_EXECUTION_CONTEXT`, `BATCH_STEP_EXECUTION`, `BATCH_STEP_EXECUTION_CONTEXT` + `*_SEQ`):** created in `fly_db` on startup (9 tables, verified present + idempotent across restarts). They store the scheduler job-execution history. Not JPA entities — managed by Spring Batch.

> Boot 4 **removed** the old `spring.batch.jdbc.initialize-schema` auto-init. Schema is now created via `spring.sql.init` pointing at Spring Batch's bundled `classpath:org/springframework/batch/core/schema-mysql.sql`, with `spring.sql.init.continue-on-error=true` so restarts are no-ops once the tables exist. Tests (H2) set `spring.sql.init.mode=never` (jobs don't run in tests).

> **JDBC JobRepository (2026-06-05 fix):** Spring Boot 4 / Spring Batch 6 default the `JobRepository` to the **in-memory `ResourcelessJobRepository`** (Boot's `BatchAutoConfiguration` is literally "Spring Batch using an in-memory store"). With that default the 4 schedulers launched jobs that ran in memory and **wrote nothing** to the `BATCH_*` tables — so the tables existed but stayed empty. `config/BatchInfrastructureConfig` (`@Configuration @EnableBatchProcessing @EnableJdbcJobRepository`) opts into the **JDBC** `JobRepository`/`JobOperator` (defaults `dataSource`/`transactionManager`/`jdbcTemplate` = Boot's beans), so every scheduled `JobOperator.start(...)` execution now persists to the MySQL `fly_db` `BATCH_*` tables. Batch metadata writes run under the existing JPA `transactionManager`. No schema change, no scheduler-logic change.

## Environment Variables And Config

| Variable / Config | Used By | Purpose | Required |
|---|---|---|---|
| `VITE_API_BASE_URL` | Frontend | Backend API base URL (default: `http://localhost:8080`) | No (has default) |
| `spring.datasource.url` | Backend | MySQL JDBC URL | Yes |
| `spring.datasource.username` | Backend | MySQL username | Yes |
| `spring.datasource.password` | Backend | MySQL password | Yes |
| `spring.jwt.secret` | Backend | JWT signing secret | Yes |
| `spring.jwt.expiration` | Backend | JWT expiry in ms (default: 86400000 = 24h) | No |
| `scheduler.job-details.candle-count` | Backend | Candles returned per job by `GET /api/jobs/{tf}/details` (code default 5; `application.yaml` currently sets 10) | No |
| `scheduler.cron.one-hour` | Backend | Hourly candle job cron, IST (default `30 0 * * * *`) | No (has yaml value) |
| `PATTERN_DETECT_1M` / `PATTERN_DETECT_5M` / `PATTERN_DETECT_15M` / `PATTERN_DETECT_1H` | Backend | Per-timeframe paper-trading pattern detection enable flags (`paper-trading.pattern-detector.enabled.*`). **Code default false; `application.yaml` enables 5m/15m/1h (1m stays false).** | No |
| `paper-trading.pattern-detector.*` | Backend | Detector tuning: `async-enabled`(true), `min-confidence`(0.60), `atr-period`(14), `pivot-left/right`(3), `duplicate-window-candles`(10), `use-volume-confirmation`(false), `conservative-stops`(false) | No |
| `paper-trading.evaluator.*` (`PT_DISCARD_STALE_ENABLED` / `PT_STALE_OPEN_MINUTES`) | Backend | Recovery discard of stale OPEN paper trades: `discard-stale-open-trades-enabled`(true), `stale-open-trade-minutes`(60) | No |
| `DTC_PAPER_1M` / `DTC_PAPER_5M` / `DTC_PAPER_15M` / `DTC_PAPER_1H` | Backend | Per-timeframe enable flags for DTC/crossover paper trades created from `emitSignal` (`paper-trading.dtc-indicator.enabled.*`). **`application.yaml` now enables all four (default `true`); code default in `PaperTradingProperties` is still `false`.** Set the env var to `false` to disable a timeframe. | No |
| `DELTA_BASE_URL` | Backend | Delta Exchange REST base URL (default `https://api.india.delta.exchange`) | No |
| `CANDLE_SYMBOL` | Backend | Candle instrument symbol (default `BTCUSD`) | No |
| `DEMO_BTCUSD_ID` | Backend | Delta product id for bracket orders in signal email | No |
| `EMAIL_ENABLED` | Backend | Global master switch for crossover signal emails (`notification.email.enabled`, default `true`) | No |
| `SIGNAL_EMAIL_1M` / `SIGNAL_EMAIL_5M` / `SIGNAL_EMAIL_15M` / `SIGNAL_EMAIL_1H` | Backend | Per-timeframe signal-email enable flags (`notification.email.signal.{one-minute,five-minute,fifteen-minute,one-hour}`). **1m currently defaults `false` (disabled); 5m/15m/1h default `true`.** | No |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USER` / `SMTP_PASS` / `SMTP_STARTTLS` | Backend | SMTP server config (`spring.mail.*`) | Only if `EMAIL_ENABLED=true` |
| `EMAIL_FROM` / `EMAIL_TO` / `EMAIL_CC` / `EMAIL_BCC` | Backend | Signal email sender/recipients | Only if `EMAIL_ENABLED=true` |

Do not store secret values here. See `application.yaml` for config keys.

## Frontend To Backend Integration

| Frontend Area | Backend Endpoint | Notes |
|---|---|---|
| `LoginPage` via `authService.login()` | `POST /api/auth/login` | Login succeeds → `setAuth(token)` stores token in localStorage and fires background fetch of `getUserDetails` → navigates to `/dashboard` immediately. `userDetails` populates in context once background fetch returns. On error: shows `ErrorResponseDto.message` inline and as toast. URL param `?error=` renders on load (used by 401 redirect). **Disabled accounts cannot log in:** `AuthServiceImpl.login` fetches the user via `findByUsernameAndEnabledTrue` for token generation, and `CustomUserDetailsService` maps `disabled(!enabled)` so `DaoAuthenticationProvider` rejects disabled users — either way the client gets a 401 (generic "Invalid username or password", no account enumeration). |
| `AuthContext` init (page refresh) | `POST /api/users/userDetails` | On app load, if token valid, decode JWT `sub` → call `getUserDetails(sub)` in background → set `userDetails` in state. `isAuthenticated` is token-based (synchronous), so `ProtectedRoute` passes immediately; userDetails fills in once fetch completes. |
| `api.ts` interceptor (401 handler) | Any protected endpoint | Request interceptor attaches the Bearer token only if it is still valid; an expired token is purged and never sent. On 401 (excluding login, excluding manual logout), it removes `token` + redirects to `/login?error=...` **only if the currently stored token is invalid** — a 401 from a stale in-flight request while a valid token exists is ignored, preserving the active session. |
| `DashboardPage` sidebar user button | No API call | Opens modal showing `userDetails` from context: `id`, `username`, `role`, `enabled`. No re-fetch on open. |
| `JobsDetailsPage` via `jobService.getJobDetails(timeframe)` | `GET /api/jobs/{timeframe}/details` | One card per job (1m/5m/15m/1h — 4 cards), config in `src/config/jobs.ts`. **No auto-polling:** all 4 cards load once on mount/page refresh (a `useRef` mount guard makes the initial load fire once even under React StrictMode's dev double-invoke); each card has its own refresh button that re-calls the API for just that job, plus a header "Refresh All" button (own `refreshingAll` flag). Fetches are skipped when `isTokenValid()` is false (logout). Per-card state map keyed by jobId: `{ data, loading, error, lastUpdated }` — a failed/errored card preserves its previous data. "View Details" opens a modal (reuses `.modal-overlay`/`.modal-box`) with full status, last crossover state, and the response `candles` (count = `scheduler.job-details.candle-count`) shown as an SVG candlestick chart + table. The chart adapts column/body width to the candle count and thins x-axis time labels (~max 12), with horizontal scroll for large counts. A collapsible "Raw JSON" `<details>` block at the bottom of the modal shows the candles as formatted JSON. The modal does not re-fetch — it renders the card's already-loaded data. Types in `src/types/jobDetails.ts` mirror the backend DTOs. |

| `PaperTradingPage` via `paperTradeService.getPaperTrades()` | `GET /api/paper-trades` | Sidebar tab "Paper Trading" (`/dashboard/paper-trading`). Loads once on mount (useRef StrictMode guard) + Refresh button; `isTokenValid()` guard. Summary cards (total/open/closed/discarded/bullish/bearish/safe/TP1/TP4/SL-closed/win-rate/safe-rate), quick-view chips (All/Open/Closed/Discarded/Safe/Stop-loss/TP4), client-side filters (timeframe/type/status[OPEN/CLOSED/DISCARDED]/pattern/safeTrade/TP-achieved/search/date-range), sticky-header scrollable table with type/status/safe badges (DISCARDED = purple) + TP-progress dots, and a View-Details modal (trade plan + detection reason + **crossover snapshot 1m/5m/15m/1h** + signal-candle OHLCV). Types in `src/types/paperTrade.ts` mirror the backend DTOs (incl. the 4 crossover-state strings). Black/orange theme reused. |

| `DashboardPage` Admin Access button via `adminService.registerUserByAdmin()` | `POST /api/admin/users/register` | Sidebar button **above** UserDetails, **always visible**; enabled only when `userDetails.role === "ADMIN"`, else disabled + greyed + tooltip "Admin access required". Click (admin) opens a modal (reuses `.modal-overlay`/`.modal-box`) with **three tabs**: **Register User** (username, password, role `<select>` USER/ADMIN → `POST /api/admin/users/register`), **Disable User** (username → `POST /api/admin/users/disable`, red action button), and **Affirmations** (static motivational text only — no API). Submit → `adminService` (central `api`, JWT via interceptor) → success toast + close; on error shows `ErrorResponseDto.message` inline + error toast (same pattern as `LoginPage`). Password/role never stored. Types in `auth.ts` (`AdminRegisterUserRequestDto`, `AdminDisableUserRequestDto`). |

CORS: Backend allows `http://localhost:5173` on all paths (`/**`).

## Paper Trading (chart-pattern detection + paper trades)

A backend feature that detects classical chart patterns on the per-timeframe candle buffers, creates **paper** trades (no live orders), evaluates them every 1m, and exposes them via a JWT API + the `PaperTradingPage` dashboard tab. Package: `com.flyingbird.crypto.papertrading`.

- **Flow:** each `*ServiceImpl.run()` (after appending the new candle) calls `PaperTradingOrchestrator.onNewClosedCandle(timeframe, snapshot)` — fully isolated (any failure logged + swallowed, never breaks a scheduler). It fans out to (a) pattern detection for that timeframe (flag-gated) and (b) — 1m only — paper-trade evaluation of all OPEN trades (runs regardless of detection flags).
- **Detection:** `PatternDetectionOrchestrator` checks the per-timeframe flag, takes an immutable `List.copyOf` snapshot, and (async by default) publishes `PatternDetectionRequestedEvent` → `@Async("patternDetectorExecutor") PatternDetectionEventListener` → `PatternDetectionProcessor` → `PatternDetector.detectPatterns(...)` (pure, no DB) → `PaperTradeCreationService` persists. Async executor core2/max4/queue500 so concurrent 1m+5m+15m events at minute 15 are all processed (none skipped). `async-enabled=false` → runs synchronously.
- **Patterns implemented (Phase 1, 12):** DOUBLE_TOP, DOUBLE_BOTTOM, HEAD_AND_SHOULDERS, INVERSE_HEAD_AND_SHOULDERS, ASCENDING_TRIANGLE, DESCENDING_TRIANGLE, BULL_FLAG, BEAR_FLAG, RISING_WEDGE_BREAKDOWN, FALLING_WEDGE_BREAKOUT, RECTANGLE_BREAKOUT_UP, RECTANGLE_BREAKDOWN_DOWN. Each uses pivots (`SwingPointUtils`, left/right=3), ATR-scaled tolerance (`AtrUtils`), prior-trend check, and latest-close breakout confirmation. **Reserved (Phase 2, enum-only, not detected):** TRIPLE_TOP/BOTTOM, BULL/BEAR_PENNANT, CUP_AND_HANDLE, INVERTED_CUP_AND_HANDLE, ROUNDING_BOTTOM/TOP.
- **Trade plan:** entry = latest close; SL from pattern structure ± ATR buffer; risk = |entry−SL|; TP1-4 = entry ± 1R..4R (`RiskRewardUtils`). Trades with risk ≤ 0 / wrong-side stop / below `min-confidence` are rejected. Math in `double` (Candle is double-based); persisted as `BigDecimal`.
- **Evaluation (`TradeEvaluator`, close-based):** before TP1 the initial stop is active; on TP1 → `safeTrade=true`, stop moved to entry (breakeven), stays OPEN; TP2/TP3 flag only (each flag flips at most once — guarded by `!isTpNAchieved()` so an already-hit level is never re-written); TP4 → CLOSED reason TP4; stop hit → CLOSED reason STOP_LOSS (`safeTrade` stays true if TP1 was reached). A single close crossing multiple levels is handled deterministically. Runs on a **single-thread** executor via `TradeEvaluationOrchestrator` (dedup by candle time) so evaluations never overlap; `@Transactional`; `OptimisticLockingFailureException` handled per-trade.
- **No-save-on-unchanged (Issue 1, 2026-06-06):** every OPEN trade is still evaluated, but the entity is mutated and `save()` called **only when at least one field actually changes** (TP/SL cross, discard, status change). A trade whose close crossed no level is left **untouched** → Hibernate dirty-checking issues **zero UPDATEs** for it (no per-minute write storm). `evaluateOne` returns `UNCHANGED/UPDATED/CLOSED/DISCARDED`; the run logs `evaluated / updated / closed / discarded / unchanged-skipped`. `lastEvaluatedAt` is now a "last state-change" timestamp — set only on a real change — not a per-run heartbeat (writing it every minute would itself dirty every row and defeat the optimization).
- **Recovery / DISCARDED (2026-06-06):** terminal status `DISCARDED` (alongside CLOSED) for trades that should not be evaluated normally after downtime/failure. Before the normal TP/SL ladder, `evaluateOne` checks: (1) **invalid fields** (missing price/SL/TP, stop on wrong side, TP ladder wrong direction) → DISCARDED `INVALID_PRICE_STATE`; (2) **staleness via an in-memory evaluator heartbeat** — `TradeEvaluatorImpl.lastRunAt` records the previous run; if the gap between two consecutive runs exceeds `paper-trading.evaluator.stale-open-trade-minutes` (evaluator runs every 1m, so a >60m gap ⇒ server downtime / evaluator stall) the run is treated as recovery and every still-OPEN trade is DISCARDED `AMBIGUOUS_RECOVERY_STATE` if the latest close is already past TP4/initial-SL, else `STALE_OPEN_TRADE`. **(Changed 2026-06-06: staleness moved off the per-trade `lastEvaluatedAt` timestamp onto this heartbeat — the old approach required writing `lastEvaluatedAt` every minute, which conflicts with Issue-1 no-save-on-unchanged. Trade-off: the heartbeat is in-memory, so it does not span a JVM restart — a single missed-then-resumed evaluation right around startup won't trigger discard; the invalid-fields check (1) still runs on every cycle regardless.)** Discard sets `closeReason/closePrice/closedAt` but **preserves TP flags + safeTrade** and does **not** set TP4/STOP_LOSS. A fresh (non-stale), valid trade still CLOSES normally on a deterministic TP4/SL cross — discard is recovery-only. Recovery runs in the evaluator's normal cycles (no startup hook → avoids candle-availability issues); DISCARDED is excluded from `findByTradeStatus(OPEN)`. New `CloseReason`s: `INVALID_PRICE_STATE`, `STALE_OPEN_TRADE`, `AMBIGUOUS_RECOVERY_STATE`, `DISCARDED_RECOVERY_GAP`. `close_reason` column widened VARCHAR(16)→40 (CREATE + idempotent `ALTER … MODIFY`, non-destructive; enums stored as STRING so adding values is safe). Config: `paper-trading.evaluator.{discard-stale-open-trades-enabled:true, stale-open-trade-minutes:60}` (`PT_DISCARD_STALE_ENABLED` / `PT_STALE_OPEN_MINUTES`).
- **Dedup (two guards, in the creation `@Transactional`):** (1) **active-pattern** — skip if an **OPEN** trade already exists for `(timeframe, patternName, tradeType)` (a still-visible pattern re-detected on later candles won't spawn another trade until the existing one CLOSES; opposite direction / other patterns / other timeframes are allowed); (2) **same-candle** — skip an identical trade for the exact signal candle (re-run/restart), backed by the DB unique constraint on `(timeframe, patternName, tradeType, candleTime)`. The persist-list is computed before touching `PaperCandle`, so a fully-duplicate batch creates no candle row. One `PaperCandle` per (timeframe, candleTime) reused for all its patterns. NO unique constraint on `tradeStatus` (that would wrongly block multiple CLOSED trades). Residual: two concurrent detections for the *same* (tf,pattern,type) could both pass the OPEN-check before commit — unlikely since one detection event fires per (timeframe, candle); a DB partial/filtered unique index would fully prevent it but MySQL lacks them (documented as future hardening).
- **PaperCandle persistence + relation (Issue 2):** the creation flow (`@Transactional`) computes the persist-list first, then **find-or-creates one `PaperCandle`** per `(timeframe, candleTime)` (`findByTimeframeAndCandleTime(...).orElseGet(save(...))`) and sets it on each new `PaperTrade` via the `@ManyToOne(optional=false)` `candle_id` relation (one candle → many trades). Candle + trades commit/rollback together. A fully-duplicate batch creates **no** candle row. The API reads candle data **only** from `paperTrade.getPaperCandle()` (mapped to nested `PaperCandleResponseDto`) — no transient/mirrored OHLCV on the trade; `candleTime` on the trade is a denormalized convenience only. (The earlier "empty `paper_candles`" was the dropped/no-trades state — the code already persisted candles; the relation is now NOT NULL to enforce it.)
- **Crossover snapshot (Issue 3):** 4 string columns on `PaperTrade` — `one/five/fifteen-minute` + `one-hour_crossover_state` — capture the latest EMA-stack signal (`BULLISH`/`BEARISH`/`NEUTRAL`) of all 4 timeframes **at creation time** (immutable snapshot; never touched by the evaluator). Source: `CrossoverStateSnapshotService` reads the 4 scheduler **stores** (`*CandleStore.crossoverSnapshot()` — leaf beans, read-locked immutable DTO, no external call, no cycle with the detection pipeline) and normalizes `null`/unknown → `NEUTRAL`. Stored as **String** (matching `SchedulerConstants.SIGNAL_*` — the project has no crossover enum, so none was invented). Captured once per creation batch, only when ≥1 trade will be created. Exposed in `PaperTradeDetailsResponseDto` + frontend type + the View-Details modal ("Crossover Snapshot (at creation)" section).
- **API:** `GET /api/paper-trades` (`PaperTradeController`, JWT, Swagger) → `PaperTradeQueryService` → `PaperTradeDetailsResponseDto` (+ nested `PaperCandleResponseDto` + 4 crossover-snapshot strings); optional in-memory filters; newest first; entities never exposed.
- **Config:** `paper-trading.pattern-detector.enabled.{one-minute,five-minute,fifteen-minute,one-hour}` all default **false** (`PATTERN_DETECT_*` env). Detection only runs where enabled; evaluation always runs. Tests: `RiskRewardUtilsTest`, `PatternDetectorTest` (double top/bottom), `TradeEvaluatorTest` (TP/SL + no-save-on-unchanged), `TradeEvaluatorRecoveryTest` (heartbeat discard), `PaperTradeCreationServiceTest` (dedup + candle/crossover) — all pass.
- **Audit (Issue 4, 2026-06-06):** `PatternDetectorImpl` + `RiskRewardUtils` reviewed — **no formula bugs found, no formulas changed.** Verified: MIN_CANDLES=40 guard; oldest→latest order; latest-close breakout confirmation (no look-ahead); `List.copyOf` defensive copies (scheduler buffer never mutated); bullish SL<entry / bearish SL>entry enforced by `RiskRewardUtils.validStopSide`; risk>0 or pattern rejected; TP1–4 strictly in trade direction (`entry ± n·R`); `finite()` guards on entry/SL/breakout catch any `lineValueAt` divide-by-zero (→ pattern skipped, no bad trade); confidence-score null-guarded. **Remaining heuristics (documented, intentionally unchanged):** confidence scoring (base 0.62 + ATR-breakout bonus), tolerance multipliers (1.5·ATR trough, tol·1.5 shoulders, 0.6 impulse ratio, 0.1·ATR breakout buffer), and the 0.5% prior-trend threshold — all reasonable defaults to be tuned via paper-trade results, not bugs.
- **DTC / crossover paper trades (2026-06-06):** in addition to chart-pattern detection, a paper trade is created whenever a scheduler's `emitSignal` fires (the NEUTRAL→BULLISH/BEARISH crossover transition) — for any of 1m/5m/15m/1h, **flag-gated** by `paper-trading.dtc-indicator.enabled.*` (`DTC_PAPER_*`, all default **false**). The logic is **inlined per scheduler** — each `*CandleServiceImpl` has its own private `createDtcPaperTrade(signal, snap)` (+ `dtc*` helpers); there is **no shared DtcPaperTradeService** (intentionally duplicated, matching the project's per-job "no shared runner" design — each scheduler runs on its own thread). It computes: entry = latest close; **BULLISH** SL = min low of last 10 candles, risk = entry−SL, TPn = entry+n·risk; **BEARISH** SL = max high of last 10, risk = SL−entry, TPn = entry−n·risk; `risk ≤ 0` → skipped + logged (BigDecimal scale-8, PatternDetector style). It builds a `ChartPatternName.DTC_INDICATOR` `PatternDetectionResultDto` and **delegates to the shared `PaperTradeCreationService.createFromDetections`** (each ServiceImpl injects `PaperTradeCreationService` + `PaperTradingProperties` directly), so it reuses the exact same persistence flow as pattern trades: active-OPEN dedup `(timeframe, DTC_INDICATOR, tradeType)` (new trade allowed only after the prior one is CLOSED/DISCARDED) + same-candle dedup, `PaperCandle` find-or-create + relation (latest candle persisted, no transient data), the 4-timeframe crossover snapshot, and one transactional save. Each `emitSignal()` calls it in a try/catch so it never breaks the scheduler/email. New enum value `DTC_INDICATOR` (metadata `direction` nominal — actual direction is per-signal). **emitSignal refactor (2026-06-06):** the old per-signal `OrderRequest`/`buildOrder` (SL from last-3 candles, TP=3R) was **removed** from all 4 `emitSignal` methods; `createDtcPaperTrade` now **returns** the `PatternDetectionResultDto` and the notification is sent **after** it, using that plan as the email payload. `MailService.sendSignalEmail` now takes a generic `Object` payload (was `OrderRequest`) so any DTO can be emailed (`buildBody` prints it under "----- TRADE PLAN -----", skipped if null). The DTC **enable flag now gates only persistence** — the plan is always computed so the signal email keeps working even when DTC trade-creation is off (default).
- **Disclaimer:** patterns are heuristics — **not** financial advice; this is paper trading only (no Delta order placement). Must be backtested before any trust.

### Job Details API contract (`GET /api/jobs/{timeframe}/details`)

- **Path var:** `timeframe` = `1m` / `5m` / `15m` / `1h` (a bare `fb_*` job id → 400). Mapped to `JobId` at the backend via `Timeframe.toJobId()`.
- **Auth:** JWT required (`anyRequest().authenticated()`); Swagger shows `Bearer Authentication`. Errors: 400 invalid timeframe, 401 no/invalid JWT, 404 job not found, 500 unexpected.
- **Response `JobDetailsResponseDto`:** `{ jobId, jobName, timeframe, status: JobStatusDto, lastCrossOverState: CrossoverStateDto|null, candles: Candle[] }` — all immutable snapshots (status RW-locked, candles copied via `subList` of the read-locked buffer; no internal collection exposed). The number of candles returned is configurable via `scheduler.job-details.candle-count` (code default 5; `application.yaml` currently sets 10). Field renamed from `lastFiveCandles` → `candles` (2026-06-02) since the count is configurable — frontend type `jobDetails.ts` matches.
- **Backend files:** `controller/JobDetailsController` (thin) → `scheduler/common/JobDetailsService`(+Impl, dispatches to the per-job services) → `JobDetailsResponseDto`. Test: `controller/JobDetailsControllerTest` (`@SpringBootTest`, verifies timeframe→JobId mapping; 3 tests pass).

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
| 2026-06-01 | Type-safe enum path variables: `/api/market/{timeframe}`→`Timeframe` (only 1m/5m/15m), `/api/scheduler/jobs/{jobId}`→`JobId` (only fb_*_job) | Each URL strictly accepts its own form (invalid → 400 via converter + MethodArgumentTypeMismatch handler). `Timeframe`↔`JobId` mapped 1:1 (`Timeframe.toJobId()` / `JobId.toTimeframe()`) so scheduler + market data correlate with no separate mapping table. |
| 2026-06-02 | Added aggregate Job Details API `GET /api/jobs/{timeframe}/details` (`JobDetailsController` → `JobDetailsService`/Impl → `JobDetailsResponseDto`) + integrated `JobsDetailsPage` (cards, per-card + Refresh All buttons, View Details modal, SVG candlestick chart) | One round-trip for the dashboard: status + last crossover + last N candles per job; thin controller, per-job dispatch, immutable snapshots |
| 2026-06-02 | Job-details candle count made configurable (`scheduler.job-details.candle-count`, code default 5, yaml set to 10); response field renamed `lastFiveCandles` → `candles` | Count is no longer fixed at 5; field name kept accurate. Frontend type + chart updated to match (chart sizing adapts to count) |
| 2026-06-02 | `JobsDetailsPage` uses no auto-polling; manual per-card refresh + header "Refresh All" | User decision — cards update on page load and on explicit refresh only (no interval timers) |
| 2026-06-02 | Added 4th scheduler job: hourly (`fb_1h_job`, `ONE_HOUR`/`1h`, thread `scheduler-1h-candle`, cron `30 0 * * * *`, Delta resolution `1h`/bucket 3600s) in new `scheduler/hourlyCandle` package (6 classes mirroring 1m) | Requirement to track hourly Delta candles using the exact per-job pattern (no base class/shared runner); reused generic `DeltaCandleClient` — no new deps, public endpoint, no secrets. Enum/switch sites (JobId, Timeframe, JobDetailsServiceImpl, MarketDataController, SchedulerStatusController history) extended for the 4th value; frontend gets a 4th card automatically via `JOB_CONFIGS` |
| 2026-06-02 | `seed()` records an initial crossover snapshot (`recordInitialCrossover()`) for all 4 jobs | Hourly crossover was null until the top-of-hour run; recording at seed populates `lastCrossOverState` right after startup. No email on seed. |
| 2026-06-02 | `JobsDetailsPage` initial load guarded by a `useRef` flag | Removes the duplicate API call per card caused by React StrictMode's dev double-invoke of effects (production was already single-call) |
| 2026-06-06 | DTC/crossover paper trades: `emitSignal` (all 4 schedulers) now creates a `DTC_INDICATOR` paper trade via new `DtcPaperTradeService` → reuses `createFromDetections` (dedup + PaperCandle + crossover snapshot). New enum `ChartPatternName.DTC_INDICATOR`; per-timeframe flags `paper-trading.dtc-indicator.enabled.*` (`DTC_PAPER_*`, default false) | Wanted paper trades from the crossover indicator, not just chart patterns. SL = min-low/max-high of last 10 candles, TP1–4 = 1R–4R; delegating to the existing creation flow avoids duplicating dedup/candle/snapshot logic (no god class). Default-off = zero behavior change until enabled; isolated in try/catch so signal email/scheduler never break. No schema change (enum is STRING, column VARCHAR(48)). |
| 2026-06-06 | ADMIN disable-user + enabled-only login: new protected `POST /api/admin/users/disable` (`AuthService.disableUser`; 404 unknown, **403 if target is ADMIN**, sets `enabled=false`, idempotent); login now fetches via `findByUsernameAndEnabledTrue` so disabled accounts get no token; Admin Access modal gains a 2nd tab (Register / Disable) | Admins can deactivate non-admin users; ADMIN accounts are protected from being disabled; disabled users can't authenticate. No schema change (reuses `users.enabled`). |
| 2026-06-06 | ADMIN-only user registration: new protected `POST /api/admin/users/register` (`AdminUserController` + `AuthService.registerByAdmin` + `AdminRegisterUserRequestDto`); frontend Admin Access sidebar button (always visible, disabled+greyed for non-admins) + Register User modal + `adminService.ts` | Let admins create users (incl. ADMINs) without exposing the public register. New protected endpoint instead of touching public `/api/auth/register` (left unchanged); ADMIN enforced by `/api/admin/**` → authenticated() (401) + `@PreAuthorize("hasRole('ADMIN')")` (403); reuses BCrypt encoder + unique-username guard; role validated USER/ADMIN via DTO `@Pattern`; no schema change |
| 2026-06-06 | Paper-trade recovery: added terminal `DISCARDED` status + recovery reasons; `TradeEvaluator` discards invalid / stale (downtime) OPEN trades before normal eval; config `paper-trading.evaluator.*`; `close_reason` widened to VARCHAR(40) | After server downtime/failure the latest price may be far past a trade's window — discarding (not closing) avoids unreliable TP/SL inference; enums stored as STRING so adding values is safe; recovery runs in the evaluator's normal cycles (no startup hook). |
| 2026-06-06 | Paper Trading feature: `papertrading` package (PatternDetector + 12 Phase-1 patterns, PaperCandle/PaperTrade entities, TradeEvaluator, async event detection, `GET /api/paper-trades`, `PaperTradingPage`) | Detect chart patterns on candle buffers → simulate trades (entry/SL/TP1-4) → evaluate every 1m. Paper only, no live orders. Schema via `spring.sql.init` (`ddl-auto` stays `none`); detection flags default OFF per timeframe; pure detector + isolated scheduler hook (never breaks jobs); single-thread evaluator; dedup via unique constraints. |
| 2026-06-05 | Per-timeframe signal-email enable flags (`notification.email.signal.*`, all default true) checked in each `*ServiceImpl.emitSignal()` | Let each job (1m/5m/15m/1h) disable its signal emails independently without touching detection logic; defaults preserve current behavior; disabled = signal still detected/recorded, just no email (not an error). Reused existing `NotificationProperties`; `MailService` contract unchanged. |
| 2026-06-05 | Added `config/BatchInfrastructureConfig` (`@EnableBatchProcessing @EnableJdbcJobRepository`) to make Spring Batch use the JDBC `JobRepository` | Boot 4 / Batch 6 default `JobRepository` is the in-memory `ResourcelessJobRepository`, so the 4 schedulers' `JobOperator.start(...)` runs wrote nothing to the `BATCH_*` tables (tables existed but empty). Opting into JDBC persists every execution to MySQL `fly_db`. No scheduler/business-logic change; tables already created by `spring.sql.init`. |
| 2026-06-04 | "Expired token sent after login" diagnosed as ENVIRONMENTAL, not a code bug | Exhaustive scan: single `token` key, one axios instance, interceptor reads token at request time, no `axios.defaults` header, no direct axios/fetch, no polling, no hardcoded/env token, no proxy header, no service worker. Incognito works → stale browser state (old cached bundle / un-cleared `localStorage` / old open tab / extension) in the normal Chrome profile. Fix = clear site data + hard reload; backend correctly rejects expired tokens. Experimental frontend `clearAuthStorage`/`markLoggedIn`/always-overwrite-interceptor + frontend/backend debug logs were tried then reverted — current committed auth = the 2026-06-04 hardening (purge-expired request interceptor + guarded 401 handler). |

## Known Issues

| Issue | Area | Status | Notes |
|---|---|---|---|
| `/admin/update-role` is public | Backend | Resolved | Secured with `@PreAuthorize("hasRole('ADMIN')")` and `permitAll()` removed from SecurityConfig on 2026-06-01 |
| Spring Batch `BATCH_*` tables empty despite jobs running | Backend | Resolved (2026-06-05) | Boot 4/Batch 6 defaulted to the in-memory `ResourcelessJobRepository`; fixed by `BatchInfrastructureConfig` (`@EnableBatchProcessing @EnableJdbcJobRepository`) → JDBC persistence. Verify with `SELECT * FROM BATCH_JOB_EXECUTION` after a scheduled run. |
| No database migration tool | Backend | Open | DDL is `none`; schema exists in MySQL (`fly_db`) but is managed manually; no Flyway/Liquibase |
| No frontend test script | Frontend | Open | `npm run test` does not exist in package.json |
| Paper-trading patterns are heuristics (false positives) | Backend/Trading | Open (by design) | The 12 detectors fire on simple measurable rules; they will produce false positives. Detection is OFF by default per timeframe. Paper trading only — no live orders. Must be backtested before any trust; intrabar high/low hit detection (vs close) is a future refinement. |
| Public `/api/auth/register` accepts an arbitrary `role` | Backend/Security | Open (pre-existing, out of scope) | The public registration endpoint (`permitAll`) lets anyone self-register, including `role: "ADMIN"`. The new admin feature does NOT use it — admin creation goes through the protected `POST /api/admin/users/register`. Recommended future hardening: force `role=USER` (or remove `role`) in the public `RegisterRequestDto`/`AuthServiceImpl.register`, or make register admin-only. Not changed here per task scope ("do not change existing public register behavior"). |
| Dashboard pages API integration | Frontend | Partial | `JobsDetailsPage` is now fully integrated with `GET /api/jobs/{tf}/details` (cards + modal + candlestick chart). `OverviewPage` still uses hardcoded mock market data; `TradesPage`, `HistoryPage`, `AnalyticsPage` are still placeholders (no API calls). |

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
Updated 2026-06-06: emitSignal refactor + generic email payload. Removed the old OrderRequest/buildOrder block (last-3-candle SL, TP=3R) from all 4 *CandleServiceImpl.emitSignal(); createDtcPaperTrade now returns PatternDetectionResultDto (was void) and the notification is sent AFTER it using that plan as the email payload (subject still BUY/SELL from signal). DTC enable flag now gates ONLY persistence — the plan is computed/returned even when DTC is off so the signal email still works (default config). MailService.sendSignalEmail(Candle, Object, String, String) (was OrderRequest); MailServiceImpl.buildBody prints the payload under "----- TRADE PLAN -----" (null-skipped). Removed now-unused OrderRequest import from the 4 schedulers. VERIFIED: backend mvnw test 32/32 BUILD SUCCESS.
Updated 2026-06-06: DTC logic inlined per scheduler (removed shared service). Deleted DtcPaperTradeService + DtcPaperTradeServiceImpl; moved the full DTC trade-plan logic (createDtcPaperTrade + dtcDirection/dtcMinLow/dtcMaxHigh/dtcTakeProfit/dtcBd + DTC_SL_LOOKBACK/DTC_SCALE constants) into EACH of the 4 *CandleServiceImpl (1m/5m/15m/1h) — intentional duplication per the per-job/no-shared-runner design (each scheduler runs on its own thread). Each ServiceImpl now injects PaperTradeCreationService + PaperTradingProperties directly and still delegates persistence to createFromDetections (dedup + PaperCandle + crossover snapshot reused). emitSignal hook unchanged (calls the local createDtcPaperTrade in try/catch). Enum DTC_INDICATOR + config flags unchanged. VERIFIED: backend mvnw test 32/32 BUILD SUCCESS.
Updated 2026-06-06: DTC/crossover paper trades. Each of the 4 *CandleServiceImpl.emitSignal() now calls new DtcPaperTradeService.createFromSignal(timeframe, signal, snap) (flag-gated by paper-trading.dtc-indicator.enabled.* / DTC_PAPER_*, default false; wrapped in try/catch so email/scheduler never break). DtcPaperTradeServiceImpl computes entry=latest close, BULLISH SL=min low of last 10 / BEARISH SL=max high of last 10, risk + TP1–4 (1R–4R) in BigDecimal scale-8, skips+logs if risk<=0 or non-directional, builds a ChartPatternName.DTC_INDICATOR PatternDetectionResultDto and delegates to PaperTradeCreationService.createFromDetections (reuses active-OPEN dedup + same-candle dedup + PaperCandle find-or-create/relation + 4-tf crossover snapshot + transactional save). Added enum DTC_INDICATOR (metadata direction nominal/unused), PaperTradingProperties.dtcIndicator + isDtcEnabled(tf), application.yaml dtc-indicator block. VERIFIED: backend mvnw test 32/32 BUILD SUCCESS (context loads, no DI cycle). Backend-only; no schema/dependency/frontend change; existing signal-email behavior unchanged.
Updated 2026-06-06: ADMIN disable-user + enabled-only login. Backend: AdminUserController gained POST /api/admin/users/disable (@PreAuthorize ADMIN + Swagger), AuthService.disableUser + impl (UserNotFoundException→404, ForbiddenAccessException→403 when target role=ADMIN, sets enabled=false, idempotent if already disabled), new AdminDisableUserRequestDto (@NotBlank username). Login hardened: UserRepository.findByUsernameAndEnabledTrue + AuthServiceImpl.login uses it so disabled accounts never get a token (CustomUserDetailsService already maps disabled(!enabled) → DaoAuthenticationProvider also rejects; both → generic 401). Frontend: Admin Access modal now has Register/Disable tabs; adminService.disableUserByAdmin; AdminDisableUserRequestDto type; index.css admin-tabs + btn-danger. VERIFIED: backend mvnw test 32/32 BUILD SUCCESS; frontend npm run build exit 0. No schema/dependency change; ADMIN accounts cannot be disabled.
Updated 2026-06-06: ADMIN-only user registration. Backend: new AdminUserController (POST /api/admin/users/register, @PreAuthorize("hasRole('ADMIN')") + @SecurityRequirement + Swagger), AuthService.registerByAdmin + impl (unique username → 409, BCrypt encode, role normalized/validated USER|ADMIN, enabled=true), new AdminRegisterUserRequestDto (@NotBlank + role @Pattern). SecurityConfig unchanged (/api/admin/** already authenticated() → 401 without token; @PreAuthorize → 403 for non-admin). Public /api/auth/register intentionally left unchanged (flagged as a Known Issue — accepts arbitrary role). Frontend: DashboardPage Admin Access sidebar button (always visible, enabled only for role===ADMIN else disabled+greyed+tooltip) + Register User modal (username/password/role select), new adminService.ts (central api/JWT interceptor), AdminRegisterUserRequestDto type, minimal index.css (disabled sb-user-btn, sb-user-sub, modal form + select). VERIFIED: backend mvnw test BUILD SUCCESS (32/32, context loads with new controller); frontend npm run build exit 0. No schema/dependency change; no weakening of security.
Audited 2026-06-06: Re-indexed (shallow-medium), read-only. Verified versions against source: Frontend React 19.2.4 / Vite 8.0.4 / TypeScript 6.0.2 (axios 1.15, react-router-dom 7.14, lucide-react 1.8); scripts dev/build/lint/preview (no test) — all match. Backend Spring Boot 4.0.5 / Java 21 / springdoc 2.7.0 / jjwt 0.11.5 / starters batch+mail+validation+security+data-jpa+web+actuator, H2 test, mysql-connector-j, no Flyway/Liquibase — all match. Confirmed the `papertrading` backend package (controller/service/impl/pattern/entity/enums/dto/event/util/config/repository — 36 files incl. new CrossoverStateSnapshotService/Impl/Dto) and frontend PaperTradingPage.tsx + paperTradeService.ts + paperTrade.ts all exist. Only fix: the Folder Structure tree predated the Paper Trading feature → added the backend `papertrading/` package + the 3 frontend files (every other section already documented Paper Trading correctly). API table, routes, scheduler, DB, env, integration all still accurate. No application code modified. Working tree: untracked papertrading/ + db/ + 3 frontend paper-trading files; modified 4 scheduler ServiceImpls + application.yaml + App.tsx + index.css + DashboardPage.tsx + project_details.md (all from the prior Paper Trading work, not this audit).
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
Updated 2026-06-02: Added 4th HOURLY scheduler job. New package scheduler/hourlyCandle (HourlyCandleScheduler/Service/ServiceImpl/Store/BatchConfig/Tasklet — exact clone of the 1m pattern; RESOLUTION 1h, bucket 3600s, thread scheduler-1h-candle, cron scheduler.cron.one-hour=30 0 * * * *, batch job hourlyCandleJob). Enums JobId(+FB_1H)/Timeframe(+ONE_HOUR) and all switch sites updated (JobDetailsServiceImpl, MarketDataController, SchedulerStatusController BATCH_JOB_NAMES). application.yaml + test application.yaml got one-hour cron. Frontend: JOB_CONFIGS gained fb_1h_job (4th card auto-renders); modal now has a collapsible Raw-JSON candle block. No new deps, no secrets (public Delta /v2/history/candles), no schema/auth change. VERIFIED: backend mvnw test BUILD SUCCESS (JobDetailsControllerTest 3/3, hourly mapping asserted, 4 schedulers register); frontend npm run build exit 0.
Updated 2026-06-06: Paper-trade recovery/discard. Added terminal PaperTradeStatus.DISCARDED + CloseReason {INVALID_PRICE_STATE, STALE_OPEN_TRADE, AMBIGUOUS_RECOVERY_STATE, DISCARDED_RECOVERY_GAP}. TradeEvaluatorImpl now (before normal TP/SL) discards invalid-field trades and stale OPEN trades (now − lastEvaluatedAt > paper-trading.evaluator.stale-open-trade-minutes, default 60; enabled by default) — AMBIGUOUS_RECOVERY_STATE if price beyond TP4/initial-SL else STALE_OPEN_TRADE; preserves TP/safeTrade flags, doesn't set TP4/SL; fresh valid trades still CLOSE normally. PaperTradingProperties.Evaluator + application.yaml config. close_reason column VARCHAR(16)→40 (entity + schema CREATE + idempotent ALTER; enums STRING so safe). Frontend PaperTradingPage: DISCARDED status filter/quick-view/summary card + purple badge. Swagger description updated. VERIFIED: backend mvnw test 21/21 (TradeEvaluatorTest 10 incl 4 discard tests); frontend npm run build exit 0. No live-trading/auth/dependency changes; no destructive DB change.
Updated 2026-06-06: Added Paper Trading feature (pattern detection + paper trades). New backend package com.flyingbird.crypto.papertrading: PatternDetector(+Impl, 12 Phase-1 detectors), enums (ChartPatternName/TradeDirection/PaperTradeStatus/CloseReason), utils (SwingPoint/PatternMath/Atr/RiskReward/CandleTime), entities PaperCandle+PaperTrade (+repositories), PaperTradeCreationService, TradeEvaluator, PatternDetectionProcessor/Orchestrator + @Async event listener + PaperTradingAsyncConfig (@EnableAsync, pattern-detector pool + single-thread evaluator), TradeEvaluationOrchestrator, PaperTradingOrchestrator (scheduler facade), PaperTradeQueryService + PaperTradeController (GET /api/paper-trades, JWT, Swagger), PaperTradingProperties. Schedulers: each *ServiceImpl.run() calls the orchestrator (isolated). Schema: db/paper-trading-schema.sql via spring.sql.init (ddl-auto stays none). Config: paper-trading.pattern-detector.* (per-tf flags default false). Frontend: PaperTradingPage + route /dashboard/paper-trading + sidebar item + paperTradeService + paperTrade types + CSS. VERIFIED: backend mvnw test BUILD SUCCESS (20/20 incl PatternDetectorTest/TradeEvaluatorTest/RiskRewardUtilsTest + full @SpringBootTest context loads); frontend npm run build exit 0.
Updated 2026-06-05: Added per-timeframe signal-email enable flags. notification.email.signal.{one-minute,five-minute,fifteen-minute,one-hour} (default true, env SIGNAL_EMAIL_{1M,5M,15M,1H}) bound in NotificationProperties.Signal + isEmailEnabledFor(resolution). Each of the 4 *ServiceImpl.emitSignal() now checks the flag before calling MailService; when false it logs and returns (signal still detected/recorded, scheduler still completes; not a failure). MailService contract + global notification.email.enabled unchanged. Added NotificationPropertiesTest (4 tests). VERIFIED: mvnw test BUILD SUCCESS (8/8). Backend-only; no frontend/Swagger/schema change.
Updated 2026-06-05: Fixed empty Spring Batch metadata tables. Root cause: Boot 4.0.5 / Batch 6.0.3 default the JobRepository to the in-memory ResourcelessJobRepository (Boot BatchAutoConfiguration = "Spring Batch using an in-memory store"), so the 4 schedulers' JobOperator.start(...) executions never persisted to MySQL BATCH_* tables (tables created by spring.sql.init but stayed empty). Fix = new config/BatchInfrastructureConfig annotated @EnableBatchProcessing + @EnableJdbcJobRepository (defaults dataSource/transactionManager/jdbcTemplate match Boot beans) → JDBC JobRepository/JobOperator now persist every execution to fly_db. No scheduler-logic/Job/Step/Tasklet change, no schema change, no new dependency. VERIFIED: mvnw test BUILD SUCCESS (4/4; full @SpringBootTest context loads with JDBC batch infra on H2). Backend-only; no frontend/Swagger change.
Audited 2026-06-05: Re-indexed (shallow-medium) on branch feature-job-details-page-design (HEAD e17f666 "UPdated one hour scheduler and JWT token fix"; working tree clean). Verified committed code: hourly job (scheduler/hourlyCandle, 6 files), recordInitialCrossover() in all 4 ServiceImpl seed()s, JobsDetailsPage 4 cards + useRef double-call guard + Refresh All + candlestick modal, resizable sidebar (DashboardPage). Auth code matches the documented 2026-06-04 hardening EXACTLY — api.ts purge-expired request interceptor + guarded 401 handler, AuthContext bootstrap purge, single 'token' key; markManualLogout present, NO markLoggedIn/clearAuthStorage, NO debug logs in frontend or backend (earlier experimental debug logging + System.out.println were reverted). Doc fixes: market read APIs tf list 1m/5m/15m→1m/5m/15m/1h; JobsDetailsPage "3 cards"→"4 cards" + mount-guard note; added seed-time initial-crossover note + 3 Known Decisions (initial crossover at seed, StrictMode mount guard, expired-token-after-login = environmental). No application code modified.
Audited 2026-06-02: Project re-indexed (shallow-medium). Verified stack unchanged — Frontend React 19.2 / Vite 8.0 / TS ~6.0 (axios 1.15, react-router-dom 7, lucide-react 1.8); Backend Java 21 / Spring Boot 4.0.5 / springdoc 2.7.0 / jjwt 0.11.5 / spring-boot-starter-batch + -mail present; routes + dashboard pages match docs. Reconciled docs with the Job Details feature shipped this session: new `GET /api/jobs/{timeframe}/details` (JobDetailsController/Service/Impl/JobDetailsResponseDto), `JobsDetailsPage` integration (cards + Refresh All + View Details modal + adaptive SVG candlestick chart), configurable `scheduler.job-details.candle-count` (code default 5; application.yaml currently 10), and response field rename `lastFiveCandles` → `candles`. Updated API table, folder structure, env/config table, Job Details contract, F→B integration, Known Decisions, Known Issues. No application code modified during this audit. Git branch: feature-job-details-page-design; working-tree changes are the Job Details work (8 files).
Updated 2026-06-01: Phase 5 — FULL per-job refactor. Removed AbstractCandleScheduler, CandleJobLauncher (shared launcher), MarketDataJobService (shared run), CandleBufferService + CandleStore/registry (shared store), EmaService/CrossoverService (folded into stateless CandleCalculationUtils), MarketDataInitializer, and Spring Batch (dependency + BatchConfig + tasklet + spring.sql.init + JobExecution/JobStatus batch DTOs). New layout: scheduler/{oneMinuteCandle,fiveMinuteCandle,fifteenMinuteCandle} each with own Scheduler+Service+ServiceImpl+Store (own ThreadPoolTaskScheduler + ReentrantReadWriteLock); scheduler/common = CandleCalculationUtils + SchedulerTimeUtils + JobStatusService(+Impl) + DTOs (JobStatusDto, JobExecutionDto, CrossoverStateDto). DeltaCandleClient now generic (resolution+bucket). /api/scheduler/history backed by in-memory per-job history. APIs/paths/auth unchanged. VERIFIED: clean compile, BUILD SUCCESS (tests, 7.5s), 3 independent named schedulers init at startup. SCHEDULER_README.md updated.
```
