# FlyingBird Scheduler Module

Functional + technical guide to the candle-data scheduler in `Backend-Java/crypto`.

The module is **fully per-job**: every scheduler job owns its own package, scheduler
class, service, data store and dedicated thread. There is **no abstract base scheduler,
no shared launcher, and no common run method**. The only shared pieces are stateless
calculation utilities and the job-status service.

---

## 1. Functional overview

Three independent jobs track crypto candles and raise EMA-crossover signals:

| Timeframe | Job ID | Runs (IST) | Dedicated thread |
|---|---|---|---|
| 1 minute | `fb_1m_job` | second 5 every minute | `scheduler-1m-candle` |
| 5 minute | `fb_5m_job` | second 5 every 5th minute | `scheduler-5m-candle` |
| 15 minute | `fb_15m_job` | second 15 every 15th minute | `scheduler-15m-candle` |

Each run: self-seed its buffer if empty → fetch the latest **closed** candle from Delta
→ append it with incrementally-computed EMAs (dedup by candle time) → evaluate the
EMA-stack crossover (`ema30 ≥ … ≥ ema60` = bullish, `≤ … ≤` = bearish) → on a
`NEUTRAL → BULLISH/BEARISH` transition build a bracket order and email it.

Jobs are independent: at **13:15** the 5m and 15m jobs run **concurrently on separate
threads**; a slow job only delays its own next tick, never another job.

### APIs (all JWT-protected, in Swagger)

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/scheduler/jobs` | live status of all jobs |
| GET | `/api/scheduler/jobs/{jobId}` | live status of one job (`fb_1m_job` …) |
| GET | `/api/scheduler/history?limit=N` | recent in-memory execution history (newest first) |
| GET | `/api/market/{tf}/crossover-state` | latest signal state (`tf` = `1m`/`5m`/`15m`) |
| GET | `/api/market/{tf}/last-candle` | most recent candle |
| GET | `/api/market/{tf}/buffer` | full candle buffer snapshot |

Status fields: `jobId, jobName, cron, threadName, running, lastStartTime, lastEndTime,
lastSuccessTime, lastFailureTime, nextRunTime, totalRuns, totalFailures, lastDurationMs,
lastDataCount, lastErrorMessage`.

---

## 2. Package layout

```
com.flyingbird.crypto.scheduler
  common/                              (ONLY shared scheduler code)
    CandleCalculationUtils.java        stateless: EMA seed/next, append-with-EMA, crossover, order
    SchedulerTimeUtils.java            stateless IST time helper
    SchedulerConstants.java            signal/execution constants
    JobStatusService.java / ...Impl    thread-safe live status + in-memory history (RW lock)
    JobStatusDto.java / JobExecutionDto.java / CrossoverStateDto.java
  oneMinuteCandle/
    OneMinuteCandleScheduler.java      own ThreadPoolTaskScheduler + cron + run()
    OneMinuteCandleService.java / OneMinuteCandleServiceImpl.java
    OneMinuteCandleStore.java          own Deque + ReentrantReadWriteLock + signal state
  fiveMinuteCandle/    (same 4 classes)
  fifteenMinuteCandle/ (same 4 classes)
```

Shared infrastructure (stateless / thread-safe, not job-specific): `marketdata/client/
DeltaCandleClient` (Delta HTTP, takes resolution + bucketSeconds), `service/MailService`
(SMTP), `marketdata/model/{Candle,OrderRequest}`, `config/{SchedulerProperties,
MarketDataProperties,NotificationProperties}`.

There is intentionally **no** `AbstractCandleScheduler`, `CandleJobLauncher`,
`MarketDataJobService`, `CandleBufferService`, or shared `CandleStore` registry.

---

## 3. Threading — one dedicated thread per job

Each `*Scheduler` is a self-contained `@Component`:

- In `@PostConstruct` it registers its job with `JobStatusService`, creates its OWN
  single-thread `ThreadPoolTaskScheduler` (thread name prefix = the job thread name), and
  schedules its cron task: `executor.schedule(this::run, new CronTrigger(cron, IST))`.
  Cron comes from `application.yaml` (`scheduler.cron.*`). **No `@Scheduled`, no shared
  scheduler bean.**
- Its private `run()` is its own (not shared): overlap guard via
  `JobStatusService.tryStart(jobId, thread)` (skip if already running — `max_instances=1`
  semantics) → call its service → record success/failure + duration + record count → log.
- `@PreDestroy` shuts the executor down.

Because each job has its own executor, jobs never block each other.

---

## 4. Concurrency & data safety

Each `*Store` owns its `Deque<Candle>` + crossover signal state behind its own
`ReentrantReadWriteLock`:

- Writes — `seedReplace`, `appendLatest`, `recordEvaluation` — take the **write lock**.
- Reads — `snapshot`, `size`, `lastCandle`, `crossoverSnapshot` — take the **read lock**
  and return **immutable copies / DTOs**. The internal deque is never exposed.
- The slow Delta HTTP fetch happens in the service **before** the store is touched, so the
  lock is held only for the in-memory mutation (never during network I/O).
- `recordEvaluation` atomically records the signal and reports whether this is a fresh
  `NEUTRAL → signal` transition, so the email decision is race-free; the email itself is
  sent **outside** the lock.
- `JobStatusService` guards status + history with a `ReentrantReadWriteLock`; APIs read
  consistent snapshots even while a job is writing.
- A failed run never corrupts data: a failed fetch throws before any mutation, so the last
  good buffer survives; the failure is recorded in status + history. One job's failure
  never stops the app or another job.

The only shared math (`CandleCalculationUtils`) is stateless (no fields) — safe to call
from all three job threads at once.

---

## 5. Configuration (`application.yaml`)

```yaml
scheduler:
  timezone: Asia/Kolkata
  cron:
    one-min: "5 * * * * *"
    five-min: "5 */5 * * * *"
    fifteen-min: "15 */15 * * * *"

market:
  delta-base-url: ${DELTA_BASE_URL:https://api.india.delta.exchange}
  symbol: ${CANDLE_SYMBOL:BTCUSD}
  buffer-length: 300
  seed-multiple: 10
  retry-attempts: 3
  retry-backoff-ms: 1000

notification:
  email:
    enabled: ${EMAIL_ENABLED:true}   # set SMTP_* + EMAIL_TO to actually send
```

---

## 6. Build / run / verify

```cmd
cd D:\Bhavin\Backend-Java\crypto
mvnw.cmd clean compile
mvnw.cmd test                   REM contextLoads (H2, hermetic)
mvnw.cmd spring-boot:run        REM needs MySQL fly_db up
```

- Startup logs show three independent schedulers, e.g.
  `[scheduler-5m-candle] scheduled | cron=5 */5 * * * * | timezone=Asia/Kolkata`.
- Each tick logs `Started/Completed/Failed N-minute candle fetch ... records=…` on the
  job's own thread.
- Swagger UI `http://localhost:8080/swagger-ui.html` — `/api/scheduler/**` and
  `/api/market/**` present with the JWT lock.
- After login: `GET /api/scheduler/jobs`, `GET /api/scheduler/history`,
  `GET /api/market/5m/crossover-state`.

## 7. History & startup behaviour

- **Live status** (`/api/scheduler/jobs`) is in-memory (`JobStatusService`, RW-locked).
- **Durable history** (`/api/scheduler/history`) uses **per-job Spring Batch**: each job owns
  its own `Job`/`Step`/`Tasklet`/`BatchConfig` (no shared batch job/runner), and each
  scheduler launches its OWN job via `JobOperator` on its own thread. All runs are recorded
  in the **one common** `BATCH_*` table set (job names `oneMinuteCandleJob` /
  `fiveMinuteCandleJob` / `fifteenMinuteCandleJob`); the controller reads them via
  `JobRepository`. Schema is auto-created on startup (`spring.sql.init` + bundled
  `schema-mysql.sql`, `continue-on-error`).
- **Startup:** each job seeds its own 300-candle buffer at startup on its own thread
  (`scheduler.seed-on-startup=true`; disabled in tests).
- **Refill on error:** if fetching the next candle throws, the job empties + refills its
  buffer with the initial seed logic; if the refill fetch also fails, the previous buffer is
  preserved and the failure is recorded.
- DI uses `@RequiredArgsConstructor`; each per-job `Job` bean is resolved by constructor
  parameter name (= bean name), so no shared launcher/qualifier is needed.
