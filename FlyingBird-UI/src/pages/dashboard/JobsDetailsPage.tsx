import React, { useCallback, useEffect, useState } from 'react';
import {
  Activity, AlertTriangle, CheckCircle2, Clock, RefreshCw, X, XCircle,
} from 'lucide-react';
import { JOB_CONFIGS, type JobConfig } from '../../config/jobs';
import { getJobDetails } from '../../services/jobService';
import { isTokenValid } from '../../services/authService';
import type { Candle, JobDetailsResponseDto } from '../../types/jobDetails';

// ─── Types ──────────────────────────────────────────────────────────
interface JobCardState {
  data: JobDetailsResponseDto | null;
  loading: boolean;
  error: string | null;
  lastUpdated: number | null;
}

type StateMap = Record<string, JobCardState>;

const EMPTY: JobCardState = { data: null, loading: true, error: null, lastUpdated: null };

// ─── Helpers ────────────────────────────────────────────────────────
const fmt = (v: string | null | undefined): string => (v && v.trim() ? v : '—');

const fmtNum = (v: number | null | undefined): string =>
  v === null || v === undefined ? '—' : v.toLocaleString();

const fmtPrice = (v: number | null | undefined): string =>
  v === null || v === undefined
    ? '—'
    : v.toLocaleString(undefined, { maximumFractionDigits: 2 });

const fmtDuration = (ms: number | null | undefined): string => {
  if (ms === null || ms === undefined) return '—';
  if (ms < 1000) return `${ms} ms`;
  return `${(ms / 1000).toFixed(2)} s`;
};

const nowLabel = (ts: number | null): string =>
  ts ? new Date(ts).toLocaleTimeString() : '—';

const sigClass = (signal: string | null | undefined): string => {
  const s = (signal ?? '').toUpperCase();
  if (s === 'BULLISH') return 'sig-bull';
  if (s === 'BEARISH') return 'sig-bear';
  return 'sig-neutral';
};

// "20260601_235500" → "23:55"
const hhmm = (t: string | null | undefined): string => {
  if (!t) return '—';
  const parts = t.split('_');
  if (parts.length === 2 && parts[1].length >= 4) {
    return `${parts[1].slice(0, 2)}:${parts[1].slice(2, 4)}`;
  }
  return t;
};

// ─── Candlestick chart (SVG, no chart library) ──────────────────────
const CandleChart: React.FC<{ candles: Candle[] }> = ({ candles }) => {
  if (!candles.length) return null;

  const PAD_LEFT = 52;
  const PAD_RIGHT = 12;
  const PAD_TOP = 12;
  const PLOT_H = 200;
  const AXIS_H = 22;
  const COL_W = 70;
  const BODY_W = 24;
  const HEIGHT = PAD_TOP + PLOT_H + AXIS_H;
  const WIDTH = PAD_LEFT + candles.length * COL_W + PAD_RIGHT;

  const highs = candles.map((c) => c.high);
  const lows = candles.map((c) => c.low);
  const max = Math.max(...highs);
  const min = Math.min(...lows);
  const range = max - min || 1;

  const y = (price: number): number => PAD_TOP + ((max - price) / range) * PLOT_H;
  const colX = (i: number): number => PAD_LEFT + i * COL_W + COL_W / 2;

  const gridLines = [max, (max + min) / 2, min];

  return (
    <div className="candle-chart-wrap">
      <svg
        className="candle-chart"
        viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
        width={WIDTH}
        height={HEIGHT}
        role="img"
        aria-label="Last candles chart"
      >
        {/* Grid + price axis */}
        {gridLines.map((price, idx) => {
          const gy = y(price);
          return (
            <g key={`grid-${idx}`}>
              <line className="cc-grid" x1={PAD_LEFT} y1={gy} x2={WIDTH - PAD_RIGHT} y2={gy} />
              <text className="cc-axis" x={PAD_LEFT - 6} y={gy + 3} textAnchor="end">
                {fmtPrice(price)}
              </text>
            </g>
          );
        })}

        {/* Candles */}
        {candles.map((c, i) => {
          const up = c.close >= c.open;
          const cx = colX(i);
          const bodyTop = y(Math.max(c.open, c.close));
          const bodyBottom = y(Math.min(c.open, c.close));
          const bodyH = Math.max(1, bodyBottom - bodyTop);
          return (
            <g key={`c-${i}`} className={up ? 'cc-up' : 'cc-down'}>
              <title>{`${hhmm(c.time)}  O:${fmtPrice(c.open)}  H:${fmtPrice(c.high)}  L:${fmtPrice(c.low)}  C:${fmtPrice(c.close)}`}</title>
              <line className="cc-wick" x1={cx} y1={y(c.high)} x2={cx} y2={y(c.low)} />
              <rect
                className="cc-body"
                x={cx - BODY_W / 2}
                y={bodyTop}
                width={BODY_W}
                height={bodyH}
                rx={1}
              />
              <text className="cc-axis" x={cx} y={HEIGHT - 6} textAnchor="middle">
                {hhmm(c.time)}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
};

// ─── Modal sections ─────────────────────────────────────────────────
const Field: React.FC<{ label: string; value: React.ReactNode }> = ({ label, value }) => (
  <div className="modal-field">
    <span className="modal-field-label">{label}</span>
    <span className="modal-field-value">{value}</span>
  </div>
);

const JobStatusSection: React.FC<{ d: JobDetailsResponseDto }> = ({ d }) => {
  const s = d.status;
  return (
    <div className="job-modal-section">
      <h4 className="job-modal-section-title">Status</h4>
      <div className="job-modal-grid">
        <Field label="Job Name" value={fmt(s?.jobName)} />
        <Field label="Job ID" value={fmt(d.jobId)} />
        <Field label="Timeframe" value={fmt(d.timeframe)} />
        <Field
          label="Running"
          value={
            <span className={`job-badge ${s?.running ? 'job-badge--on' : 'job-badge--off'}`}>
              {s?.running ? 'Running' : 'Idle'}
            </span>
          }
        />
        <Field label="Cron" value={fmt(s?.cron)} />
        <Field label="Thread" value={fmt(s?.threadName)} />
        <Field label="Last Start" value={fmt(s?.lastStartTime)} />
        <Field label="Last End" value={fmt(s?.lastEndTime)} />
        <Field label="Last Success" value={fmt(s?.lastSuccessTime)} />
        <Field label="Last Failure" value={fmt(s?.lastFailureTime)} />
        <Field label="Next Run" value={fmt(s?.nextRunTime)} />
        <Field label="Total Runs" value={fmtNum(s?.totalRuns)} />
        <Field label="Total Failures" value={fmtNum(s?.totalFailures)} />
        <Field label="Last Duration" value={fmtDuration(s?.lastDurationMs)} />
        <Field label="Last Data Count" value={fmtNum(s?.lastDataCount)} />
      </div>
      {s?.lastErrorMessage ? (
        <div className="job-modal-error">
          <AlertTriangle size={14} /> {s.lastErrorMessage}
        </div>
      ) : null}
    </div>
  );
};

const CrossoverSection: React.FC<{ d: JobDetailsResponseDto }> = ({ d }) => {
  const x = d.lastCrossOverState;
  return (
    <div className="job-modal-section">
      <h4 className="job-modal-section-title">Last Crossover State</h4>
      {x ? (
        <div className="job-modal-grid">
          <Field
            label="Signal"
            value={
              <span className={`signal-badge ${sigClass(x.lastSignal)}`}>{fmt(x.lastSignal)}</span>
            }
          />
          <Field label="Calls" value={fmtNum(x.calls)} />
          <Field label="Last Candle Time" value={fmt(x.lastCandleTime)} />
          <Field label="Last Checked (IST)" value={fmt(x.lastCheckedAtIst)} />
        </div>
      ) : (
        <p className="job-empty">No crossover state evaluated yet.</p>
      )}
    </div>
  );
};

const CandlesSection: React.FC<{ candles: Candle[] }> = ({ candles }) => (
  <div className="job-modal-section">
    <h4 className="job-modal-section-title">Last {candles.length || 5} Candles</h4>
    {candles.length ? (
      <>
        <CandleChart candles={candles} />
        <div className="candle-table-wrap">
          <table className="candle-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Open</th>
                <th>High</th>
                <th>Low</th>
                <th>Close</th>
                <th>Vol</th>
                <th>Type</th>
              </tr>
            </thead>
            <tbody>
              {candles.map((c, i) => (
                <tr key={`${c.time}-${i}`}>
                  <td>{hhmm(c.time)}</td>
                  <td>{fmtPrice(c.open)}</td>
                  <td>{fmtPrice(c.high)}</td>
                  <td>{fmtPrice(c.low)}</td>
                  <td>{fmtPrice(c.close)}</td>
                  <td>{fmtPrice(c.volume)}</td>
                  <td>
                    <span className={`candle-type ${c.candleType === 'red' ? 'is-red' : 'is-green'}`}>
                      {fmt(c.candleType)}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </>
    ) : (
      <p className="job-empty">No candles available yet.</p>
    )}
  </div>
);

// ─── Card ───────────────────────────────────────────────────────────
const JobCard: React.FC<{
  job: JobConfig;
  state: JobCardState;
  onView: () => void;
  onRefresh: () => void;
}> = ({ job, state, onView, onRefresh }) => {
  const { data, loading, error, lastUpdated } = state;
  const s = data?.status;
  const running = !!s?.running;

  return (
    <div className="job-card">
      <div className="job-card-head">
        <div className="job-card-title">
          <Activity size={16} className="job-card-icon" />
          <span>{job.label}</span>
        </div>
        <span className="tf-badge">{job.timeframe}</span>
      </div>

      <div className="job-card-statusline">
        <span className={`job-badge ${running ? 'job-badge--on' : 'job-badge--off'}`}>
          {running ? 'Running' : 'Idle'}
        </span>
        <span className="job-card-updated">
          <Clock size={11} /> {nowLabel(lastUpdated)}
        </span>
      </div>

      {error ? (
        <div className="job-card-error">
          <AlertTriangle size={14} /> {error}
        </div>
      ) : (
        <>
          <div className="job-stat-row">
            <div className="job-stat">
              <span className="job-stat-label">Total Runs</span>
              <span className="job-stat-value">{fmtNum(s?.totalRuns)}</span>
            </div>
            <div className="job-stat">
              <span className="job-stat-label">Last Success</span>
              <span className="job-stat-value job-stat-value--sm">
                <CheckCircle2 size={12} className="ok" /> {fmt(s?.lastSuccessTime)}
              </span>
            </div>
          </div>

          <div className="job-stat-row">
            <div className="job-stat">
              <span className="job-stat-label">Total Failures</span>
              <span className="job-stat-value">{fmtNum(s?.totalFailures)}</span>
            </div>
            <div className="job-stat">
              <span className="job-stat-label">Last Failure</span>
              <span className="job-stat-value job-stat-value--sm">
                <XCircle size={12} className="bad" /> {fmt(s?.lastFailureTime)}
              </span>
            </div>
          </div>

          <div className="job-signal-line">
            <span className="job-stat-label">Crossover Signal</span>
            <span className={`signal-badge ${sigClass(data?.lastCrossOverState?.lastSignal)}`}>
              {fmt(data?.lastCrossOverState?.lastSignal)}
            </span>
          </div>
        </>
      )}

      <div className="job-card-actions">
        <button className="job-refresh-btn" onClick={onRefresh} title="Refresh now" disabled={loading}>
          <RefreshCw size={14} className={loading ? 'spin' : ''} />
        </button>
        <button className="job-view-btn" onClick={onView} disabled={!data}>
          View Details
        </button>
      </div>
    </div>
  );
};

// ─── Page ───────────────────────────────────────────────────────────
const JobsDetailsPage: React.FC = () => {
  const [states, setStates] = useState<StateMap>(() =>
    Object.fromEntries(JOB_CONFIGS.map((j) => [j.jobId, { ...EMPTY }])),
  );
  const [openJobId, setOpenJobId] = useState<string | null>(null);
  const [refreshingAll, setRefreshingAll] = useState(false);

  const fetchJob = useCallback(async (job: JobConfig) => {
    if (!isTokenValid()) return; // skip when unauthenticated / logged out
    setStates((prev) => ({ ...prev, [job.jobId]: { ...prev[job.jobId], loading: true } }));
    try {
      const data = await getJobDetails(job.timeframe);
      setStates((prev) => ({
        ...prev,
        [job.jobId]: { data, loading: false, error: null, lastUpdated: Date.now() },
      }));
    } catch (err: unknown) {
      // Preserve previous valid data; only surface the error.
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Failed to load job details.';
      setStates((prev) => ({
        ...prev,
        [job.jobId]: { ...prev[job.jobId], loading: false, error: message },
      }));
    }
  }, []);

  // "Refresh All" has its own loading flag so a single-card refresh never makes
  // this button spin/disable.
  const refreshAll = useCallback(async () => {
    setRefreshingAll(true);
    try {
      await Promise.all(JOB_CONFIGS.map((job) => fetchJob(job)));
    } finally {
      setRefreshingAll(false);
    }
  }, [fetchJob]);

  // Load all 3 cards once on mount / page refresh. No auto-polling — each card
  // is refreshed on demand via its own refresh button or the "Refresh All" button.
  useEffect(() => {
    refreshAll();
  }, [refreshAll]);

  const openJob = openJobId ? states[openJobId] : null;
  const openConfig = JOB_CONFIGS.find((j) => j.jobId === openJobId) ?? null;

  return (
    <div className="page-content jobs-page">
      <div className="welcome-row">
        <div>
          <h2 className="welcome-title">Jobs Details</h2>
          <p className="welcome-sub">Live status of the candle scheduler jobs.</p>
        </div>
        <button className="job-refresh-all-btn" onClick={refreshAll} disabled={refreshingAll}>
          <RefreshCw size={14} className={refreshingAll ? 'spin' : ''} />
          Refresh All
        </button>
      </div>

      <div className="job-grid">
        {JOB_CONFIGS.map((job) => (
          <JobCard
            key={job.jobId}
            job={job}
            state={states[job.jobId]}
            onView={() => setOpenJobId(job.jobId)}
            onRefresh={() => fetchJob(job)}
          />
        ))}
      </div>

      {openJobId && openJob?.data && openConfig && (
        <div
          className="modal-overlay"
          onClick={() => setOpenJobId(null)}
          role="dialog"
          aria-modal="true"
          aria-label="Job Details"
        >
          <div className="modal-box modal-box--wide" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>
                {openConfig.label} <span className="tf-badge">{openConfig.timeframe}</span>
              </h3>
              <button
                className="modal-close-btn"
                onClick={() => setOpenJobId(null)}
                aria-label="Close"
              >
                <X size={16} />
              </button>
            </div>
            <div className="modal-body modal-body--scroll">
              <JobStatusSection d={openJob.data} />
              <CrossoverSection d={openJob.data} />
              <CandlesSection candles={openJob.data.lastFiveCandles} />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default JobsDetailsPage;
