// Frontend mirror of the backend scheduler DTOs returned by
// GET /api/jobs/{timeframe}/details. Field names/shape match the Java DTOs.

/** Mirror of backend JobStatusDto. Date fields are "yyyy-MM-dd HH:mm:ss" strings. */
export interface JobStatusDto {
  jobId: string;
  jobName: string;
  cron: string | null;
  threadName: string | null;
  running: boolean;
  lastStartTime: string | null;
  lastEndTime: string | null;
  lastSuccessTime: string | null;
  lastFailureTime: string | null;
  nextRunTime: string | null;
  totalRuns: number;
  totalFailures: number;
  lastDurationMs: number | null;
  lastDataCount: number | null;
  lastErrorMessage: string | null;
}

/** Mirror of backend CrossoverStateDto. */
export interface CrossoverStateDto {
  jobId: string;
  calls: number;
  lastSignal: string | null;       // BULLISH / BEARISH / NEUTRAL / null
  lastCandleTime: string | null;   // IST string
  lastCheckedAtIst: string | null;
}

/** Mirror of backend Candle (in-memory market-data model). */
export interface Candle {
  time: string;                    // IST "yyyyMMdd_HHmmss"
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  ema21: number | null;
  ema30: number | null;
  ema35: number | null;
  ema40: number | null;
  ema45: number | null;
  ema50: number | null;
  ema60: number | null;
  ema200: number | null;
  candleType: string | null;       // "green" | "red"
}

/** Mirror of backend JobDetailsResponseDto. */
export interface JobDetailsResponseDto {
  jobId: string;
  jobName: string;
  timeframe: string;               // 1m / 5m / 15m
  status: JobStatusDto | null;
  lastCrossOverState: CrossoverStateDto | null;
  lastFiveCandles: Candle[];
}
