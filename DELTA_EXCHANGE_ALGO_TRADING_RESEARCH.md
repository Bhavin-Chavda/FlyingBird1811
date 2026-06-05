# Delta Exchange India Algo Trading Research

## 1. Research Date

- **2026-06-05**
- Researcher: design/architecture pass only (no code).
- Project context: FlyingBird crypto dashboard — Spring Boot 4 / Java 21 backend with a per-job candle scheduler (1m/5m/15m/1h) that already fetches public Delta India candles via `marketdata/client/DeltaCandleClient` (`GET /v2/history/candles`, India base URL, no auth) and computes an EMA-stack crossover signal. This document plans the *trading* layer on top of that.

## 2. Scope And Non-Implementation Note

- This is a **research + architecture** document only. **Nothing here is implemented.**
- No project code, frontend, backend, database, dependencies, config, or `project_details.md` were modified to produce it.
- All API facts are sourced from the **official Delta Exchange documentation** (see §34). Any field/path that could not be fully confirmed from official docs is marked **`Needs confirmation`** and must be re-verified against the live docs before coding.
- Endpoint shapes evolve — **treat the live docs as the source of truth at implementation time**, not this snapshot.

## 3. Important Safety Disclaimer

- **Crypto derivatives (perpetual futures) are high-risk and leveraged.** Losses can exceed intuition and positions can be liquidated.
- **No strategy in this document is claimed to be profitable.** Every strategy idea is a hypothesis that **MUST be backtested and paper-traded** before any live use.
- **Do not present any internet/social claim as certain.** Non-official sources are marked as *supporting research*.
- **Auto-trading must be off by default.** Live order placement should require explicit per-stage opt-in, hard capital/loss caps, and a manual-approval phase first.
- **Never commit or log API secrets.** Use placeholders (`<DELTA_API_KEY>`, `<DELTA_API_SECRET>`) and externalized config only.

## 4. Delta Exchange API Overview

- **Production base URL (India):** `https://api.india.delta.exchange`
- **Testnet base URL (India):** `https://cdn-ind.testnet.deltaex.org` *(Needs confirmation — testnet hosts change; verify before use)*
- **API style:** REST, JSON. Most responses wrap payloads as `{ "success": true, "result": ... }` (the project's `DeltaCandleClient` already models `DeltaResponse(success, result)`).
- **Market-data reads** (products, tickers, orderbook, candles) are **public** (no signing). The project already relies on this for candles.
- **Account/trading endpoints** (orders, positions, wallet, leverage, fills, profile) **require signed authentication** (§5) plus, typically, **IP whitelisting**.
- **WebSocket** feeds exist for live ticker/orderbook/positions (out of scope for the first phases; polling on candle ticks is sufficient initially).

### Endpoint quick map (verify paths against live docs before coding)

| Category | Method | Path | Auth |
|---|---|---|---|
| Products list | GET | `/v2/products` | Public |
| Product by symbol | GET | `/v2/products/{symbol}` | Public |
| All tickers | GET | `/v2/tickers` | Public |
| Ticker by symbol | GET | `/v2/tickers/{symbol}` | Public |
| L2 orderbook | GET | `/v2/orderbook?product_id={id}` *(also seen as `/v2/l2orderbook/{symbol}` — confirm)* | Public |
| Historical candles | GET | `/v2/history/candles?symbol&resolution&start&end` | Public |
| Place order | POST | `/v2/orders` | Signed |
| Active orders | GET | `/v2/orders` | Signed |
| Order by id | GET | `/v2/orders/{id}` *(confirm)* | Signed |
| Order by client id | GET | `/v2/orders/client_order_id/{coid}` | Signed |
| Edit order | PUT | `/v2/orders` | Signed |
| Cancel order | DELETE | `/v2/orders` | Signed |
| Cancel all orders | DELETE | `/v2/orders/all` | Signed |
| Batch create/edit/delete | POST/PUT/DELETE | `/v2/orders/batch` (max 50) | Signed |
| Positions | GET | `/v2/positions` (and `/v2/positions/margined` — confirm) | Signed |
| Change leverage | POST | `/v2/products/{product_id}/orders/leverage` | Signed |
| Wallet balances | GET | `/v2/wallet/balances` | Signed |
| Fills/executions | GET | `/v2/fills` | Signed |
| User/profile | GET | `/v2/user` *(or `/v2/profile` — confirm)* | Signed |

## 5. Authentication And Signing Requirements

**Signing scheme (official):** HMAC-SHA256, hex-encoded, with your **API secret** as the key, over the concatenated string:

```
signature_data = method + timestamp + requestPath + queryString + body
signature      = hex( HMAC_SHA256( apiSecret, signature_data ) )
```

- `method` — uppercase HTTP verb, e.g. `GET`, `POST`.
- `timestamp` — current **Unix time in seconds** (string).
- `requestPath` — the path only, e.g. `/v2/orders` (no host).
- `queryString` — for GET with query params, the exact query string (e.g. `?product_id=27`); empty string if none. **Sign exactly what you send.**
- `body` — the JSON request body string for POST/PUT/DELETE; empty string for GET.

**Required headers:**

| Header | Value |
|---|---|
| `api-key` | `<DELTA_API_KEY>` |
| `timestamp` | unix seconds used in the signature |
| `signature` | hex HMAC-SHA256 result |
| `User-Agent` | a client identifier (required; e.g. `flyingbird-java`) |
| `Content-Type` | `application/json` (for POST/PUT/DELETE) |

**Critical rules / gotchas:**
- **Signature expiry:** a signature is only valid for **~5 seconds** after generation. → The server **clock-sync matters**; keep system time accurate (NTP). Build the timestamp immediately before sending.
- **GET vs POST signing:** GET signs `method+timestamp+path+queryString` (empty body); POST/PUT/DELETE signs `method+timestamp+path+queryString+body`. The body string used to sign **must be byte-identical** to the body sent.
- **IP whitelisting:** API keys are typically restricted to whitelisted IPs — the server/host running the bot must be whitelisted.
- **Permissions/scope:** keys can be created read-only or with trading permission; request the **minimum scope** per phase (read-only first).
- **Secret handling:** secret is never sent; it only keys the HMAC. Never log it, never expose to the frontend.

**Java/Spring pseudocode (design only — do NOT implement here):**

```text
class DeltaSigner {
  String sign(String method, String path, String query, String body, long tsSeconds) {
    String data = method + tsSeconds + path + (query==null?"":query) + (body==null?"":body);
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(apiSecret.getBytes(UTF_8), "HmacSHA256"));
    return Hex.encode(mac.doFinal(data.getBytes(UTF_8)));
  }
}
// A signed RestClient interceptor adds api-key, timestamp, signature, User-Agent
// using the SAME path/query/body it is about to send.
```

**Future config keys (placeholders, never real secrets):**

```yaml
delta:
  exchange:
    base-url: https://api.india.delta.exchange
    api-key: ${DELTA_API_KEY:}
    api-secret: ${DELTA_API_SECRET:}
    btc-product-symbol: ${DELTA_BTC_SYMBOL:BTCUSD}
    request-timeout-ms: 10000
    signature-expiry-seconds: 5
    ip-whitelisted: ${DELTA_IP_WHITELISTED:false}
```

## 6. Required User Account Details

### Required credentials
- **API key** (`<DELTA_API_KEY>`) and **API secret** (`<DELTA_API_SECRET>`) from the Delta India account.
- **Account / subaccount identifier** if trading a subaccount (`Needs confirmation` whether subaccounts apply).
- **Environment**: production vs testnet selection.
- **IP whitelist**: the public IP(s) of the server running the bot must be registered on the key.
- **API permission scope**: read-only vs trading-enabled (request minimum needed per phase).

### Required trading settings (collected/stored as config, not secrets)
- Preferred **BTC product/contract** (e.g. `BTCUSD` perpetual) and its `product_id`.
- **Margin mode** (isolated vs cross) — `Needs confirmation` on Delta's API control for this.
- **Leverage** (capped low to start).
- **Max capital allocated** to the bot (hard cap, independent of wallet balance).
- **Max risk per trade** (e.g. 0.25%–1% of allocated capital).
- **Max daily loss** (kill-switch threshold).
- **Max open positions** (recommend 1 to start) and **max open orders**.
- **Allowed order types** and **allowed timeframes**.
- **Trading enabled/disabled** master flag (default disabled).
- **Email/alert settings** (reuse existing `notification.email.*`).
- **Paper-trading vs live-trading** flag (default paper).

### Required safety confirmations (explicit user acknowledgements before live)
- User confirms **derivatives/leverage risk**.
- User confirms the **API key permissions and IP whitelist** are correct.
- User confirms the **max risk per trade and max daily loss** values.
- User confirms whether **auto order placement** is enabled (vs manual approval).
- User confirms whether the strategy may **close/modify positions automatically**.

## 7. Market Data APIs

> All public (no signing). The project already uses `/v2/history/candles`.

**7.1 Products / contracts** — `GET /v2/products`, `GET /v2/products/{symbol}`
- Purpose: discover tradable instruments, map `symbol` → `product_id`, read tick size, contract value, maker/taker fees, leverage limits.
- Use in app: resolve `BTCUSD` → `product_id`; cache product spec at startup; read fee/tick/contract-size for sizing & fee math.

**7.2 Tickers / quotes** — `GET /v2/tickers`, `GET /v2/tickers/{symbol}`
- Purpose: current price (mark/last), best bid/ask, 24h stats, **funding rate** (for perps).
- Use in app: "current BTC price", spread check (no-trade filter), funding awareness.

**7.3 Orderbook** — `GET /v2/orderbook?product_id={id}` *(confirm vs `/v2/l2orderbook/{symbol}`)*
- Purpose: L2 depth (bids/asks).
- Use in app: spread/liquidity no-trade filter, slippage estimate before market orders.

**7.4 Recent trades** — `GET /v2/trades/{symbol}` *(Needs confirmation of exact path)*
- Purpose: recent public prints (microstructure / volume confirmation).

**7.5 Historical candles (already integrated)** — `GET /v2/history/candles`
- Query: `symbol` (e.g. `BTCUSD`), `resolution` ∈ `{1m,5m,15m,1h,4h,1d}`, `start`/`end` in **unix seconds**.
- Response: array of OHLCV rows; the project filters to **closed** candles (`time < currentBucketStart`), sorts ascending, maps to `Candle`.
- Use in app: the 4 schedulers already pull 1m/5m/15m/1h — reuse for multi-timeframe confirmation (no new endpoint needed).

**Example (safe placeholders):**
```
GET https://api.india.delta.exchange/v2/history/candles?symbol=BTCUSD&resolution=5m&start=1717500000&end=1717503600
-> { "success": true, "result": [ { "time": 1717500000, "open": 67000, "high": 67120, "low": 66950, "close": 67080, "volume": 1234 }, ... ] }
```

## 8. Account And Balance APIs

> All signed (§5).

- **Wallet balances** — `GET /v2/wallet/balances`: available balance, blocked/margin, currency. Use: capital base, margin checks, max-daily-loss tracking.
- **Positions** — `GET /v2/positions` (and `/v2/positions/margined` — confirm): size, entry price, margin, **unrealized PnL**, **liquidation price**, leverage. Use: position monitoring, risk, "already-in-position" no-trade filter.
- **Profile / user** — `GET /v2/user` *(or `/v2/profile`)*: account id, KYC/trading permissions. Use: verify trading enabled.
- **Order history** — `GET /v2/orders/history` *(confirm path)*: closed/cancelled orders. Use: audit/reconciliation.
- **Trade/fills history** — `GET /v2/fills`: executions with **price, size, fee, side, timestamp**. Use: realized fills, fee accounting, PnL reconciliation.
- **PnL** — derive from positions (unrealized) + fills (realized); a dedicated PnL endpoint is `Needs confirmation`.
- **Risk/contract limits** — read from `GET /v2/products/{symbol}` (leverage band, position limits) and any account risk-limit endpoint (`Needs confirmation`).

## 9. Product And Contract APIs

- `GET /v2/products` / `GET /v2/products/{symbol}` provide: `id` (product_id), `symbol`, `contract_type` (e.g. `perpetual_futures`), `contract_value`, `tick_size`, `maker_commission_rate`, `taker_commission_rate`, leverage limits, settlement currency, status.
- **Symbol→product mapping:** look up `BTCUSD` (or the chosen perpetual) once and cache `product_id`; all order/position/leverage calls use `product_id`.
- **Important:** contract value and tick size drive **position sizing** and **price rounding**; fees come from the product spec (see §13).

## 10. Order Placement APIs

**Endpoint:** `POST /v2/orders` (signed). Batch: `POST /v2/orders/batch` (≤50).

**Request body fields (confirm against live docs):**

| Field | Type | Notes |
|---|---|---|
| `product_id` | int | required (or `product_symbol`) |
| `size` | int | contracts/lots (integer; depends on contract) |
| `side` | string | `buy` / `sell` |
| `order_type` | string | `market_order` / `limit_order` |
| `limit_price` | string | required for limit orders |
| `time_in_force` | string | `gtc` / `ioc` / `fok` |
| `post_only` | bool | maker-only (rejected if it would take) |
| `reduce_only` | bool | only reduces an open position |
| `client_order_id` | string | idempotency tag, max 32 chars |
| `stop_order_type` | string | `stop_loss_order` / `take_profit_order` (for stop/conditional) |
| `stop_price` | string | trigger price |
| `stop_trigger_method` | string | e.g. `last_traded_price` / `mark_price` |
| `trail_amount` | string | trailing-stop distance (presence ⇒ trailing) |
| `bracket_stop_loss_price` | string | attached SL trigger |
| `bracket_stop_loss_limit_price` | string | attached SL limit |
| `bracket_take_profit_price` | string | attached TP trigger |
| `bracket_take_profit_limit_price` | string | attached TP limit |
| `bracket_trail_amount` | string | bracket trailing distance |
| `bracket_stop_trigger_method` | string | bracket trigger ref price |

**Order types and how they map to our engine:**
- **Market order** (`order_type=market_order`): immediate fill; used for confirmed-signal entries and emergency exits. Taker fee + slippage.
- **Limit order** (`limit_order` + `limit_price`): price control; use `post_only` for maker-fee entries; risk of non-fill.
- **Stop / stop-market** (`stop_order_type` + `stop_price`, no limit): trigger → market. Server-side protective stop.
- **Stop-limit** (`stop_order_type` + `stop_price` + `limit_price`): trigger → limit (risk of non-fill in fast moves).
- **Take-profit** (`stop_order_type=take_profit_order`): server-side TP.
- **Bracket / OCO**: attach SL+TP to an entry via `bracket_*` fields (the project's `OrderRequest` already carries `bracketStopLossPrice`/`bracketTakeProfitPrice`). **Strongly preferred** so a protective stop exists the instant the entry fills.
- **Reduce-only / post-only**: flags on the above for safe exits / maker entries.
- **Trailing stop**: via `trail_amount` (or `bracket_trail_amount`).

**Failure cases to handle:** insufficient margin, leverage/risk-limit exceeded, price outside band, post-only would-cross rejection, product halted/expired, rate-limit (429), signature expired/invalid (401), IP not whitelisted, duplicate `client_order_id`.

**Example (placeholders):**
```
POST /v2/orders   (signed)
{ "product_id": 27, "size": 1, "side": "buy", "order_type": "market_order",
  "client_order_id": "fb-1m-1717500000",
  "bracket_stop_loss_price": "66800", "bracket_take_profit_price": "67500" }
-> { "success": true, "result": { "id": 123456, "state": "open", "size": 1, "side": "buy", ... } }
```

## 11. Order Management APIs

- **Open orders** — `GET /v2/orders` (filter by product/state).
- **Order by id** — `GET /v2/orders/{id}` *(confirm)*; **by client id** — `GET /v2/orders/client_order_id/{coid}`.
- **Cancel** — `DELETE /v2/orders` (by id/product in body); **Cancel all** — `DELETE /v2/orders/all` (optional filters: product, limit/stop/reduce-only).
- **Edit/replace** — `PUT /v2/orders` (modify price/size/stop). **SL/TP update** = edit the bracket/stop order (or cancel-replace).
- **Partial exit** = place a `reduce_only` order for part of the position; **close** = `reduce_only` order for full size, or a position-close action.
- Map: an `OrderManagementService` tracks our `client_order_id` → exchange order id, reconciles state from `GET /v2/orders` + `/v2/fills`.

## 12. Position Management APIs

- **Current position** — `GET /v2/positions` (size, entry, margin, unrealized PnL, liquidation price, leverage).
- **Close position** — submit a `reduce_only` market/limit order for the full size (a dedicated close endpoint is `Needs confirmation`).
- **Reduce position** — `reduce_only` partial order.
- **Change leverage** — `POST /v2/products/{product_id}/orders/leverage` body `{ "leverage": "5" }` (do this **before** opening, when flat).
- **Margin mode** (isolated/cross) and **add/remove position margin** — `Needs confirmation` of exact endpoints.
- **Liquidation price / maintenance margin** — read from positions/product; maintain a **liquidation buffer** so stops trigger well before liquidation.

## 13. Fees, Funding, Slippage, And Charges

- **Maker/taker fees:** example BTCUSD ≈ **maker 0.02%**, **taker 0.05%** (from product spec). **Fees vary by product and tier — fetch the actual `maker_commission_rate`/`taker_commission_rate` from `GET /v2/products/{symbol}` and/or confirm the current schedule before live trading.** `Needs confirmation` of exact current India rates.
- **Funding rate (perpetuals):** periodic funding paid/received between longs/shorts; available on the ticker/product. Matters for positions held across funding times.
- **Slippage:** market orders fill across the book — estimate from `/v2/orderbook` and add a **slippage buffer** to expected entry/exit.
- **Leverage impact:** amplifies PnL and fees-relative-to-margin; raises liquidation risk. Keep leverage low initially.
- **Cost handling rule:** include **taker fee (entry) + taker fee (exit) + expected slippage + funding** in (a) **backtests**, (b) **position sizing** (net of costs), and (c) the **minimum-RR gate** (a 1:1.5 gross RR can be much worse net of round-trip fees on small moves).
- **Where to get fees:** `GET /v2/products/{symbol}` per product; config placeholders below let you pin conservative values until confirmed:

```yaml
trading:
  fees:
    maker-rate: ${DELTA_MAKER_RATE:0.0002}
    taker-rate: ${DELTA_TAKER_RATE:0.0005}
    slippage-buffer-bps: ${DELTA_SLIPPAGE_BPS:5}
```

## 14. API Request/Response Structures

- **Envelope:** `{ "success": boolean, "result": <object|array>, "error": <object?> }` (matches the project's existing `DeltaCandleClient.DeltaResponse`).
- **Numbers as strings:** prices/sizes are often **strings** in requests (e.g. `"limit_price": "67000"`) — preserve precision; round to `tick_size`.
- **Timestamps:** candle `time` and request `timestamp` are **unix seconds** (project already uses seconds).
- **Errors:** non-2xx with `success:false` and an `error` object (code + context). 401 = auth/signature/expiry/IP; 429 = rate limit; 4xx = validation (margin, price band, post-only cross). **Map these to typed exceptions** in a `GlobalExceptionHandler`-style layer; never expose secrets or raw upstream errors to the frontend.
- **Idempotency:** `client_order_id` (≤32) is the dedup key — generate deterministically per signal (e.g. `fb-{tf}-{candleTimeEpoch}`) so a retried place doesn't double-fill.

## 15. Java Spring Boot Architecture Design

> Design only. Mirrors the existing package style (`marketdata/`, `scheduler/`, `config/`, interface+Impl services, `@ConfigurationProperties`, per-job RW-locked stores). Nothing below is implemented.

Proposed `com.flyingbird.crypto.trading` package tree:

```
trading/
  client/
    DeltaSignedClient        // signed RestClient wrapper (adds api-key/timestamp/signature/User-Agent)
    DeltaSigner              // HMAC-SHA256 signing service (§5)
    DeltaApiException        // typed errors (auth/rate-limit/validation)
  marketdata/                // reuse existing DeltaCandleClient + add ticker/orderbook reads
  account/
    AccountService / Impl    // balances, profile, positions snapshot (read-only)
  order/
    OrderService / Impl      // place/cancel/edit/get; idempotent via client_order_id
    OrderManagementService   // reconcile state from /v2/orders + /v2/fills
  position/
    PositionService / Impl   // positions, change leverage, close/reduce
    PositionMonitorService   // per-candle: hold / move-SL / trail / close
  risk/
    RiskManagementService    // pre-trade gate: caps, daily loss, sizing, liquidation buffer
    RiskState                // RW-locked counters (daily loss, open positions, consec losses)
  strategy/
    StrategyEvaluationService    // multi-timeframe rules → Decision
    SignalEvaluationService      // reuses CandleCalculationUtils crossover per timeframe
  execution/
    TradeExecutionService    // orchestrates: signal → risk gate → (paper|live) order
    PaperTradingEngine       // simulates fills/fees/SL/TP
    TradeAuditService        // append-only audit log of every decision/action
  config/
    DeltaExchangeProperties  // delta.exchange.*
    TradingProperties        // trading.* (mode, caps, fees, per-tf enable)
```

**Cross-cutting design requirements:**
1. **Delta API client / signing** — one signed client + one signer; market-data reads stay public.
2. **Paper mode (default) vs live mode** — a single `trading.mode: paper|live` flag routes `TradeExecutionService` to `PaperTradingEngine` or `OrderService`. Live also requires `trading.enabled: true` + `delta.exchange.ip-whitelisted: true`.
3. **Scheduler integration** — the existing per-job `run()` already produces a crossover transition; the trading layer hooks in **after** `recordEvaluation` (where `emitSignal` is today). Each candle tick also drives `PositionMonitorService` (manage open trades). Keep the existing per-job thread isolation; the trading calls run on the job's own thread, **never** the main app thread.
4. **Thread-safety** — `RiskState` and any open-order/position cache behind `ReentrantReadWriteLock` (same pattern as the candle stores); never hold a lock during a network call (fetch outside the lock, mutate inside).
5. **Idempotency / dup prevention** — deterministic `client_order_id`; a "one in-flight order per (job, signal)" guard mirroring the existing `tryStart` overlap guard; reconcile before re-placing.
6. **Retry rules** — retry only **idempotent reads** and **safe** failures (timeouts, 5xx, 429 with backoff). **Never blind-retry a place-order** on an ambiguous failure — first query by `client_order_id` to see if it landed, then decide.
7. **Rate-limit handling** — budget against the 10,000-units/5-min quota with per-endpoint weights (§Rate limits); centralize in the signed client; back off on 429.
8. **Audit logging** — append-only record of every signal, gate result, order request/response (no secrets), fill, and management action; this is the compliance/debug backbone and the source for PnL reconciliation.
9. **Error handling** — typed exceptions → structured responses; an email/disable failure must never crash a scheduler run (same philosophy as the current `MailServiceImpl`).
10. **Swagger/OpenAPI** — any new read/admin API stays **JWT-protected** (reuse `SecurityConfig` `anyRequest().authenticated()`), documented with `@Operation`/`@ApiResponses`/`@SecurityRequirement`; **never** expose order placement publicly; never return secrets.

## 16. Future Config Keys

```yaml
delta:
  exchange:
    base-url: https://api.india.delta.exchange
    api-key: ${DELTA_API_KEY:}
    api-secret: ${DELTA_API_SECRET:}
    btc-product-symbol: ${DELTA_BTC_SYMBOL:BTCUSD}
    request-timeout-ms: 10000
    ip-whitelisted: ${DELTA_IP_WHITELISTED:false}

trading:
  enabled: ${TRADING_ENABLED:false}        # master live switch (default OFF)
  mode: ${TRADING_MODE:paper}              # paper | live
  max-capital: ${TRADING_MAX_CAPITAL:0}
  risk-per-trade-pct: ${TRADING_RISK_PCT:0.005}    # 0.5%
  min-risk-reward: ${TRADING_MIN_RR:1.5}
  max-daily-loss-pct: ${TRADING_MAX_DAILY_LOSS:0.03}
  max-open-positions: ${TRADING_MAX_POS:1}
  max-leverage: ${TRADING_MAX_LEVERAGE:3}
  max-consecutive-losses: ${TRADING_MAX_CONSEC_LOSS:3}
  per-timeframe-enabled:
    one-minute: ${TRADE_1M:false}
    five-minute: ${TRADE_5M:false}
    fifteen-minute: ${TRADE_15M:false}
    one-hour: ${TRADE_1H:false}
  fees:
    maker-rate: ${DELTA_MAKER_RATE:0.0002}
    taker-rate: ${DELTA_TAKER_RATE:0.0005}
    slippage-buffer-bps: ${DELTA_SLIPPAGE_BPS:5}
```

(No secrets in YAML — all via env; defaults are safe/off.)

## 17. Future Database Tables

> Schema changes are **out of scope now** — listed for planning. The app already has Spring Batch `BATCH_*` tables (JDBC) for scheduler-execution history.

| Table | Purpose | Key columns (indicative) |
|---|---|---|
| `trade` | one logical trade (entry→exit) | id, timeframe, side, entry_price, exit_price, size, sl, tp, status, realized_pnl, fees, opened_at, closed_at, mode(paper/live) |
| `trade_order` | each exchange order for a trade | id, trade_id, client_order_id, exchange_order_id, type, side, price, size, state, created_at |
| `position_snapshot` | periodic position/risk capture | id, ts, size, entry, mark, unrealized_pnl, liquidation_price, margin |
| `risk_event` | gate decisions / kill-switch trips | id, ts, type, reason, blocked(bool), context_json |
| `paper_fill` | simulated fills in paper mode | id, trade_id, ts, price, size, fee, slippage |
| `signal_log` | every signal + multi-TF confirmation result | id, ts, timeframe, signal, confirmations_json, acted(bool) |

(Use Flyway/Liquibase when DB changes are approved — the project currently has no migration tool.)

## 18. Future Backend APIs (all JWT-protected; read-only first)

| Method | Path (proposed) | Purpose | Phase |
|---|---|---|---|
| GET | `/api/trading/account/balances` | wallet balances (proxy, sanitized) | 2 |
| GET | `/api/trading/positions` | open positions | 2 |
| GET | `/api/trading/orders` | open/recent orders | 2 |
| GET | `/api/trading/trades?limit=N` | trade history (our DB) | 3 |
| GET | `/api/trading/signals?limit=N` | recent signals + confirmation | 3 |
| GET | `/api/trading/config` | current trading flags (no secrets) | 1 |
| POST | `/api/trading/paper/run` | force a paper evaluation (admin/test) | 3 |
| POST | `/api/trading/approve/{signalId}` | manual-approval execution | 4 |
| POST | `/api/trading/kill-switch` | disable trading immediately | 5 |

Order **placement** is never a public/unauthenticated API; manual-approval and kill-switch must be ADMIN-gated.

## 19. Strategy Research Overview

- Available data: closed candles for **1m / 5m / 15m / 1h** (already fetched), plus an EMA-stack **crossover state** (BULLISH/BEARISH/NEUTRAL) per timeframe.
- Core idea: **multi-timeframe confirmation** — only act on a lower-timeframe signal when higher timeframes agree/support.
- **Every strategy below is a hypothesis.** Classification:
  - *Well-known general concept* — widely used, still must be validated on this instrument/data.
  - *Requires backtesting* — all of them, before paper, before live.
  - *Risky/unproven* — flagged explicitly.
  - *Not recommended* — flagged explicitly.
- **No profitability is claimed or implied.**

## 20. Candidate Strategy 1: Multi-Timeframe Crossover Confirmation

- **Name:** MTF Crossover Confirmation. **Class:** well-known concept; requires backtesting.
- **Market condition:** trending.
- **Entry:** 1m crossover transition to BULLISH (or BEARISH) **AND** 5m, 15m, 1h crossover state is the same direction or supportive (not opposing).
- **MTF rules:** require ≥ the 15m and 1h to agree; treat NEUTRAL as "not opposing" (configurable). Block on any opposing higher TF.
- **Stop loss:** below the recent swing low (long) / above swing high (short), or ATR-based (e.g. 1.5×ATR(14) on the entry timeframe).
- **Take profit:** RR-based (e.g. 1.5–2.0×risk) or prior structure level.
- **RR:** ≥ 1:1.5 **net of fees/slippage**.
- **Position sizing:** fixed-fractional (§26).
- **Max risk/trade:** 0.25–1% allocated. **Max daily loss:** §25.
- **Order type:** bracket market entry (SL+TP attached) or limit+post_only entry with attached bracket.
- **Cancel entry:** if confirmation breaks before fill (limit), or signal flips.
- **Update/trail SL:** move to breakeven after +1R; trail by ATR or swing after +1.5R.
- **Early close:** opposing higher-TF crossover, or 1h flips against the position.
- **Data:** 1m/5m/15m/1h candles + EMA stack (have it). **APIs:** candles (have), products, orders, positions, fills.
- **Pros:** filters low-quality 1m noise; aligns with trend. **Cons:** fewer trades, later entries; whipsaw in chop. **Failure modes:** all-TF-agree at exhaustion (top/bottom), correlated false signals, news spikes. **Backtest/paper:** mandatory across regimes (trend/chop/high-vol).

## 21. Candidate Strategy 2: Trend Continuation Pullback

- **Name:** Trend Continuation Pullback. **Class:** well-known; requires backtesting.
- **Market condition:** established higher-TF trend.
- **Entry:** 1h/15m bullish; wait for a 1m/5m **pullback** to end (e.g. crossover returns to bullish after a dip / pullback into a moving average) then enter long (mirror for short).
- **MTF rules:** higher TF defines direction; lower TF times the entry.
- **SL:** below the pullback swing low. **TP:** prior high / RR target.
- **RR:** ≥ 1:1.5 net. **Sizing/risk/daily loss:** §25–§26.
- **Order type:** limit at pullback zone (post_only) + bracket, or market on confirmation.
- **Cancel:** trend invalidated (higher TF flips) before fill. **Trail:** after new high beyond entry structure. **Early close:** higher TF flips.
- **Pros:** better RR than chasing breakouts; trades with trend. **Cons:** "pullback" definition is subjective → needs precise, testable rules; trend can end at the pullback. **Failure modes:** catching a reversal mislabeled as a pullback. **Backtest/paper:** mandatory.

## 22. Candidate Strategy 3: Breakout With Higher Timeframe Filter

- **Name:** Filtered Breakout. **Class:** well-known; requires backtesting.
- **Market condition:** range→expansion.
- **Entry:** 15m/1h defines a range/trend; a lower-TF (1m/5m) **breakout** of the range high/low confirms entry **only in the higher-TF direction**.
- **False-breakout filters:** candle **body** beyond level (not just wick), optional **volume** confirmation if reliable, re-test hold.
- **SL:** back inside the range / other side of the breakout candle. **TP:** measured move / RR.
- **Order type:** stop/stop-limit at the breakout level (server-side) + bracket; or market on close beyond level.
- **Cancel:** breakout fails / reclaims range. **Trail:** as the expansion extends.
- **Pros:** captures expansion moves. **Cons:** **false breakouts are common in crypto**; slippage on fast breaks. **Failure modes:** stop-hunts, low-liquidity fakeouts. **Backtest/paper:** mandatory; test the body/volume filters explicitly.

## 23. Candidate Strategy 4: Controlled Mean Reversion

- **Name:** Controlled Mean Reversion. **Class:** **risky on leveraged crypto — include only with strong risk controls.**
- **Why risky:** blind mean reversion (fading moves) fights the trend; on leverage a single sustained trend can liquidate a counter-trend position. Crypto trends/squeezes are violent.
- **If used at all:** only when the **higher TF is range-bound (not trending)**; small size; tight, **mandatory** stop; hard max loss; no averaging down; avoid during high volatility/news.
- **Entry:** extreme from mean (e.g. band/quantile) **with a higher-TF range filter**. **SL:** beyond the extreme (mandatory). **TP:** the mean. **RR:** modest; fees matter more for small moves.
- **Pros:** works in ranges. **Cons/Failure:** catastrophic in a breakout/trend; easily fee-negative. **Recommendation:** **defer**; only after the trend strategies are validated, and never without a trend/range filter + hard caps. **Backtest/paper:** mandatory and skeptical.

## 24. Candidate Strategy 5: No-Trade Filters

A trade should be **blocked** (signal logged, no order) when any of these hold:

- **Conflicting timeframes** — a higher TF opposes the signal.
- **Low liquidity / wide spread** — orderbook spread above a threshold.
- **High volatility spike** — ATR/range spike beyond a cap (avoid chaotic fills).
- **Candle anomaly** — abnormal wick/gap/outlier vs recent candles.
- **API instability** — recent Delta errors/timeouts, stale candle data.
- **Max daily loss reached** — kill-switch tripped.
- **Position/order already open** for the instrument (max-open-positions=1 to start).
- **Recent stop-loss hit** — cooldown after a loss (avoid revenge entries).
- **Insufficient balance/margin** — pre-trade margin check fails.
- **Outside chosen trading windows** (optional) / near funding time (optional).
- **Trading disabled** or **per-timeframe trade flag off** or **paper mode** (when live not approved).

These are not optional — they are the **first gate** before sizing/placing.

## 25. Risk Management Rules (production-grade)

- **Fixed-fractional risk:** risk a fixed % of *allocated* capital per trade (not wallet, not notional).
- **Risk per trade:** **0.25%–1%** (start at the low end).
- **Minimum RR:** ≥ **1:1.5** (prefer 1:2) **net of round-trip fees + slippage**.
- **Max daily loss:** e.g. **3%** of allocated → trip kill-switch, stop new entries for the day.
- **Max consecutive losses:** e.g. **3** → pause/cooldown.
- **Max open positions:** **1** to start.
- **Max leverage:** low (e.g. **≤3×**); leverage ≠ position size.
- **Notional exposure cap:** absolute cap independent of leverage.
- **Liquidation buffer:** size/leverage so the **stop triggers well before** the liquidation price.
- **Protective stop is mandatory and immediate:** use bracket orders so SL exists the moment the entry fills; in paper mode simulate it.
- **No averaging down**, **no martingale**, no adding to losers (unless a separately designed, backtested scale-in plan exists).
- **Slippage + fees + funding** always included in sizing and the RR gate.
- **Kill-switch** must be one call/flag and must also stop the schedulers from acting.

## 26. Position Sizing Formula

Risk-based sizing (design; round to contract/tick rules):

```
riskCapital      = allocatedCapital * riskPerTradePct          // e.g. 10000 * 0.005 = 50
stopDistance     = |entryPrice - stopPrice|                    // price units
estRoundTripCost = (entryPrice + exitPrice)*takerRate + slippageBuffer   // include fees+slippage
sizeRaw          = riskCapital / (stopDistance * valuePerPricePointPerContract)
size             = floorToContractStep(sizeRaw)
// reject trade if: size < minSize, OR required margin > availableMargin,
//                  OR notional > exposureCap, OR netRR < minRiskReward
netRR            = (takeProfitDistance*valuePerPoint*size - estRoundTripCost)
                 / (stopDistance*valuePerPoint*size + estRoundTripCost)
```

- `valuePerPricePointPerContract` derives from the product's `contract_value`/`tick_size` (read from `/v2/products`).
- **Size is capped by the binding constraint** (risk, margin, exposure, exchange limits) — take the minimum.

## 27. Stop Loss And Take Profit Framework

- **SL placement:** structure (swing high/low) or volatility (k×ATR); whichever is wider/safer, capped by max-risk.
- **TP placement:** RR multiple of SL distance, or next structural level; consider **partial TP** (e.g. take 50% at 1R, trail the rest).
- **Breakeven move:** after +1R, move SL to entry (± fees) to make the trade risk-free.
- **Trailing:** after +1.5R, trail by ATR or by swing points (or `trail_amount`/`bracket_trail_amount` server-side).
- **Always server-side when possible** (bracket/stop orders) so protection survives app/network outages; mirror in our own monitor for redundancy.
- **Time stop (optional):** exit if the trade hasn't worked within N candles.

## 28. Trade Lifecycle Design

```
candle close (per timeframe)
  -> SignalEvaluationService: crossover state per TF
  -> StrategyEvaluationService: MTF confirmation -> Decision(enter/none)
  -> No-Trade Filters (§24)  --blocked--> log signal_log + risk_event, stop
  -> RiskManagementService: size + caps + margin + netRR gate --fail--> log, stop
  -> mode == paper ? PaperTradingEngine.simulate(...) : OrderService.place(bracket)
  -> persist trade + trade_order; audit
On each subsequent candle / monitor tick:
  -> PositionMonitorService: reconcile fills/position
       -> move SL to breakeven / trail / partial TP / full close per rules
       -> opposing higher-TF flip -> early close
  -> update trade + audit
On exit (SL/TP/manual/kill-switch):
  -> reconcile fills, compute realized PnL net of fees, close trade row
```

- Hooks into the **existing** per-job scheduler `run()` (after `recordEvaluation`), preserving thread isolation and the overlap guard.

## 29. Backtesting Requirements

- **Historical data:** pull 1m/5m/15m/1h history via `/v2/history/candles` (already integrated) over multiple regimes (trend up/down, chop, high-vol events).
- **Cost-accurate:** model **taker fee + slippage + funding**; never assume mid-price fills for market orders.
- **No look-ahead:** only use **closed** candles (project already filters to closed) and information available at decision time; align multi-TF timestamps correctly (don't use a not-yet-closed 1h candle when acting on a 1m close).
- **Metrics:** net PnL, max drawdown, win rate, avg RR, profit factor, expectancy, trades/day, longest losing streak, sensitivity to parameters.
- **Robustness:** walk-forward / out-of-sample testing; parameter sweeps; avoid overfitting (few parameters, stable across windows).
- **Pass bar:** a strategy must show **positive net expectancy with acceptable drawdown out-of-sample** before paper trading — and even then, paper first.

## 30. Paper Trading Requirements

- **Simulate** order placement, fills (with slippage from orderbook), fees, SL/TP, trailing — using **live market data** but no real orders.
- **Compare** simulated decisions/outcomes vs the live market over a meaningful period and trade count.
- **Validate** the full pipeline end-to-end (signal → filters → sizing → simulated execution → monitoring → exit → PnL) including the kill-switch and no-trade filters.
- **Reconcile** paper PnL with what a real fill would have been (latency, partial fills).
- **Exit criteria to advance:** stable behavior, no logic bugs, results consistent with backtest expectations, all risk controls firing correctly.

## 31. Live Trading Safety Checklist

Before enabling **any** live order:
- [ ] API key has only the **needed** scope; **IP whitelisted**; secret in env (never committed/logged).
- [ ] `trading.enabled=true` **and** `trading.mode=live` are **explicit**, deliberate, and ADMIN-gated.
- [ ] **Hard caps set:** max capital, risk/trade, max daily loss, max leverage, max-open-positions=1, exposure cap.
- [ ] **Kill-switch** verified (one action disables trading and stops scheduler actions).
- [ ] **Bracket/protective stop** attached on every entry (verified in paper).
- [ ] Backtest **and** paper trading **passed** with the exact parameters going live.
- [ ] **Small size** only (well under caps); one position at a time.
- [ ] Clock synced (NTP) — signature 5s expiry.
- [ ] Alerts wired (email) for entries/exits/errors/kill-switch.
- [ ] Reconciliation/audit verified against `/v2/fills`.
- [ ] User has signed the **safety confirmations** (§6).

## 32. Suggested Implementation Roadmap

- **Phase 1 — API research & config only:** add `delta.exchange.*` config; verify product symbol/id; fetch **public** market data (products/tickers/orderbook) — **no auth, no trading.**
- **Phase 2 — Account read-only:** implement the **signed client** + signer; call balances/positions/open-orders/fills **read-only**; no order placement. Verify signing + IP whitelist end-to-end.
- **Phase 3 — Paper trading:** `PaperTradingEngine` simulating orders/fees/SL/TP; run strategies against live data; persist `signal_log`/`trade`/`paper_fill`; compare signals vs outcomes.
- **Phase 4 — Manual-approval trading:** signal → order **suggestion** surfaced (UI/email) → user **manually approves** → place a single small order. No autonomous placement.
- **Phase 5 — Small-size live (auto):** strict capital cap, strict max daily loss, **one position at a time**, kill-switch armed.
- **Phase 6 — Automated trade management:** auto SL move/trail/partial-TP/close + continuous risk monitoring, still under all caps.

Advance a phase **only** after the prior phase is verified.

## 33. Open Questions Before Implementation

1. **Credentials/permissions:** Will the user provide a Delta India API key/secret, and with what scope (read-only first)? Which **IP(s)** will run the bot (for whitelisting)?
2. **Instrument:** Which exact contract — `BTCUSD` perpetual? Confirm `product_id`, `contract_value`, `tick_size`, leverage band.
3. **Testnet:** Is a Delta India **testnet** account available for Phases 2–3? Confirm the testnet base URL.
4. **Capital & risk:** allocated capital, risk-per-trade %, max daily loss %, max leverage, exposure cap — exact numbers.
5. **Fees:** confirm current **maker/taker** rates and **funding** mechanics for the chosen contract (fetch from `/v2/products` + confirm schedule).
6. **Endpoint confirmations:** exact paths for orderbook (`/v2/orderbook` vs `/v2/l2orderbook/{symbol}`), profile (`/v2/user` vs `/v2/profile`), order-by-id, positions variants, margin-mode/add-margin, recent-trades — verify against live docs.
7. **Margin mode:** isolated vs cross, and whether the API controls it.
8. **DB & migrations:** approval to add trading tables and a migration tool (Flyway/Liquibase) when Phase 3 begins.
9. **Autonomy policy:** is autonomous placement allowed at all, or manual-approval only? Who can flip the kill-switch?
10. **Subaccounts:** single account or subaccount isolation for the bot?

## 34. Sources

**Official Delta Exchange (primary — API truth):**
- Delta Exchange API docs (Introduction & API section): https://docs.delta.exchange/
- Delta Exchange Global API docs: https://docs-global.delta.exchange/
- Delta India — "Kickstarting Your Trading Journey with Delta India APIs" (support): https://www.delta.exchange/support/solutions/articles/80001174969-kickstarting-your-trading-journey-with-delta-india-apis
- Delta India production base URL: `https://api.india.delta.exchange`

**Supporting research (NOT official API truth — for concepts/examples only):**
- Delta Exchange Global "Kickstarting … Global APIs": https://global.delta.exchange/support/solutions/articles/80001175322-kickstarting-your-trading-journey-with-delta-exchange-global-apis
- Community Python example (third-party, unverified): https://www.profitaddaweb.com/2025/04/delta-exchange-api-in-python.html
- Community wrapper (third-party, unverified): https://github.com/SirCharan/Delta

> Endpoint paths/fields marked `Needs confirmation` above must be re-verified against https://docs.delta.exchange/ at implementation time. General trading/risk concepts (fixed-fractional risk, RR, ATR stops, MTF confirmation) are standard industry practice and still require backtesting on this instrument/data; no profitability is guaranteed.
