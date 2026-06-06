import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  RefreshCw, AlertTriangle, X, Search, Shield, TrendingUp, TrendingDown,
} from 'lucide-react';
import { getPaperTrades } from '../../services/paperTradeService';
import { isTokenValid } from '../../services/authService';
import type { PaperTrade } from '../../types/paperTrade';

// ─── Helpers ─────────────────────────────────────────────────────────
const num = (v: number | null | undefined): string =>
  v === null || v === undefined ? '—' : v.toLocaleString(undefined, { maximumFractionDigits: 2 });

const fmt = (s: string | null | undefined): string => (s && s.trim() ? s : '—');
const datePart = (s: string | null | undefined): string => (s ? s.slice(0, 10) : '');
const prettyPattern = (p: string): string =>
  p.toLowerCase().split('_').map((w) => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');

type QuickView = 'all' | 'open' | 'closed' | 'discarded' | 'safe' | 'sl' | 'tp4';

interface Filters {
  timeframe: string;
  tradeType: string;
  status: string;
  pattern: string;
  safeTrade: string;   // '', 'true', 'false'
  tpAchieved: string;  // '', 'tp1'..'tp4'
  search: string;
  fromDate: string;
  toDate: string;
}

const EMPTY_FILTERS: Filters = {
  timeframe: '', tradeType: '', status: '', pattern: '', safeTrade: '',
  tpAchieved: '', search: '', fromDate: '', toDate: '',
};

// ─── TP progress pills ───────────────────────────────────────────────
const TpProgress: React.FC<{ t: PaperTrade }> = ({ t }) => (
  <div className="pt-tp-progress">
    {[t.tp1Achieved, t.tp2Achieved, t.tp3Achieved, t.tp4Achieved].map((on, i) => (
      <span key={i} className={`pt-tp-dot${on ? ' on' : ''}`} title={`TP${i + 1}${on ? ' achieved' : ''}`}>
        {i + 1}
      </span>
    ))}
  </div>
);

// ─── Summary card ────────────────────────────────────────────────────
const Card: React.FC<{ label: string; value: React.ReactNode; tone?: string }> = ({ label, value, tone }) => (
  <div className="pt-card">
    <span className="pt-card-label">{label}</span>
    <span className={`pt-card-value${tone ? ` ${tone}` : ''}`}>{value}</span>
  </div>
);

// ─── Page ────────────────────────────────────────────────────────────
const PaperTradingPage: React.FC = () => {
  const [trades, setTrades] = useState<PaperTrade[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<number | null>(null);
  const [quickView, setQuickView] = useState<QuickView>('all');
  const [filters, setFilters] = useState<Filters>(EMPTY_FILTERS);
  const [openTrade, setOpenTrade] = useState<PaperTrade | null>(null);

  const fetchTrades = useCallback(async () => {
    if (!isTokenValid()) return;
    setLoading(true);
    try {
      const data = await getPaperTrades();
      setTrades(data);
      setError(null);
      setLastUpdated(Date.now());
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Failed to load paper trades.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  const didLoad = useRef(false);
  useEffect(() => {
    if (didLoad.current) return;
    didLoad.current = true;
    fetchTrades();
  }, [fetchTrades]);

  const patternOptions = useMemo(
    () => Array.from(new Set(trades.map((t) => t.patternName))).sort(),
    [trades],
  );

  // Summary over ALL trades.
  const summary = useMemo(() => {
    const total = trades.length;
    const open = trades.filter((t) => t.tradeStatus === 'OPEN').length;
    const closed = trades.filter((t) => t.tradeStatus === 'CLOSED').length;
    const discarded = trades.filter((t) => t.tradeStatus === 'DISCARDED').length;
    const bullish = trades.filter((t) => t.tradeType === 'BULLISH').length;
    const bearish = trades.filter((t) => t.tradeType === 'BEARISH').length;
    const safe = trades.filter((t) => t.safeTrade).length;
    const tp1 = trades.filter((t) => t.tp1Achieved).length;
    const tp4 = trades.filter((t) => t.tp4Achieved).length;
    const slClosed = trades.filter((t) => t.tradeStatus === 'CLOSED' && t.closeReason === 'STOP_LOSS').length;
    const winRate = closed > 0 ? Math.round((tp4 / closed) * 100) : 0;
    const safeRate = total > 0 ? Math.round((safe / total) * 100) : 0;
    return { total, open, closed, discarded, bullish, bearish, safe, tp1, tp4, slClosed, winRate, safeRate };
  }, [trades]);

  // Filtered list (quick view + filters).
  const filtered = useMemo(() => {
    return trades.filter((t) => {
      // quick view
      if (quickView === 'open' && t.tradeStatus !== 'OPEN') return false;
      if (quickView === 'closed' && t.tradeStatus !== 'CLOSED') return false;
      if (quickView === 'discarded' && t.tradeStatus !== 'DISCARDED') return false;
      if (quickView === 'safe' && !t.safeTrade) return false;
      if (quickView === 'sl' && !(t.tradeStatus === 'CLOSED' && t.closeReason === 'STOP_LOSS')) return false;
      if (quickView === 'tp4' && !t.tp4Achieved) return false;
      // filters
      if (filters.timeframe && t.timeframe !== filters.timeframe) return false;
      if (filters.tradeType && t.tradeType !== filters.tradeType) return false;
      if (filters.status && t.tradeStatus !== filters.status) return false;
      if (filters.pattern && t.patternName !== filters.pattern) return false;
      if (filters.safeTrade && String(t.safeTrade) !== filters.safeTrade) return false;
      if (filters.tpAchieved) {
        const flag =
          filters.tpAchieved === 'tp1' ? t.tp1Achieved :
          filters.tpAchieved === 'tp2' ? t.tp2Achieved :
          filters.tpAchieved === 'tp3' ? t.tp3Achieved : t.tp4Achieved;
        if (!flag) return false;
      }
      if (filters.fromDate && datePart(t.candleTime) < filters.fromDate) return false;
      if (filters.toDate && datePart(t.candleTime) > filters.toDate) return false;
      if (filters.search) {
        const q = filters.search.toLowerCase();
        const hay = `${t.tradeId} ${t.patternName} ${t.detectionReason ?? ''}`.toLowerCase();
        if (!hay.includes(q)) return false;
      }
      return true;
    });
  }, [trades, quickView, filters]);

  const setFilter = (key: keyof Filters, value: string) =>
    setFilters((f) => ({ ...f, [key]: value }));

  const quickViews: { key: QuickView; label: string }[] = [
    { key: 'all', label: 'All' },
    { key: 'open', label: 'Open' },
    { key: 'closed', label: 'Closed' },
    { key: 'discarded', label: 'Discarded' },
    { key: 'safe', label: 'Safe' },
    { key: 'sl', label: 'Stop-loss' },
    { key: 'tp4', label: 'TP4' },
  ];

  return (
    <div className="page-content pt-page">
      <div className="welcome-row">
        <div>
          <h2 className="welcome-title">Paper Trading</h2>
          <p className="welcome-sub">
            Auto-detected chart patterns simulated as paper trades — no live orders.
            {lastUpdated ? ` Updated ${new Date(lastUpdated).toLocaleTimeString()}.` : ''}
          </p>
        </div>
        <button className="job-refresh-all-btn" onClick={fetchTrades} disabled={loading}>
          <RefreshCw size={14} className={loading ? 'spin' : ''} />
          Refresh
        </button>
      </div>

      {/* Summary cards */}
      <div className="pt-cards">
        <Card label="Total" value={summary.total} />
        <Card label="Open" value={summary.open} tone="pt-blue" />
        <Card label="Closed" value={summary.closed} />
        <Card label="Discarded" value={summary.discarded} tone="pt-purple" />
        <Card label="Bullish" value={summary.bullish} tone="pt-green" />
        <Card label="Bearish" value={summary.bearish} tone="pt-red" />
        <Card label="Safe" value={summary.safe} tone="pt-green" />
        <Card label="TP1 hit" value={summary.tp1} />
        <Card label="TP4 hit" value={summary.tp4} tone="pt-green" />
        <Card label="SL closed" value={summary.slClosed} tone="pt-red" />
        <Card label="Win rate" value={`${summary.winRate}%`} tone="pt-green" />
        <Card label="Safe rate" value={`${summary.safeRate}%`} />
      </div>

      {/* Quick views */}
      <div className="pt-quickviews">
        {quickViews.map((q) => (
          <button
            key={q.key}
            className={`pt-chip${quickView === q.key ? ' pt-chip--active' : ''}`}
            onClick={() => setQuickView(q.key)}
          >
            {q.label}
          </button>
        ))}
      </div>

      {/* Filters */}
      <div className="pt-filters">
        <div className="pt-search">
          <Search size={14} />
          <input
            type="text"
            placeholder="Search pattern, reason, trade id…"
            value={filters.search}
            onChange={(e) => setFilter('search', e.target.value)}
          />
        </div>
        <select value={filters.timeframe} onChange={(e) => setFilter('timeframe', e.target.value)}>
          <option value="">All timeframes</option>
          <option value="1m">1m</option><option value="5m">5m</option>
          <option value="15m">15m</option><option value="1h">1h</option>
        </select>
        <select value={filters.tradeType} onChange={(e) => setFilter('tradeType', e.target.value)}>
          <option value="">All types</option>
          <option value="BULLISH">Bullish</option><option value="BEARISH">Bearish</option>
        </select>
        <select value={filters.status} onChange={(e) => setFilter('status', e.target.value)}>
          <option value="">All status</option>
          <option value="OPEN">Open</option><option value="CLOSED">Closed</option>
          <option value="DISCARDED">Discarded</option>
        </select>
        <select value={filters.pattern} onChange={(e) => setFilter('pattern', e.target.value)}>
          <option value="">All patterns</option>
          {patternOptions.map((p) => <option key={p} value={p}>{prettyPattern(p)}</option>)}
        </select>
        <select value={filters.safeTrade} onChange={(e) => setFilter('safeTrade', e.target.value)}>
          <option value="">Safe: any</option>
          <option value="true">Safe only</option><option value="false">Not safe</option>
        </select>
        <select value={filters.tpAchieved} onChange={(e) => setFilter('tpAchieved', e.target.value)}>
          <option value="">TP: any</option>
          <option value="tp1">TP1+</option><option value="tp2">TP2+</option>
          <option value="tp3">TP3+</option><option value="tp4">TP4</option>
        </select>
        <input type="date" value={filters.fromDate} onChange={(e) => setFilter('fromDate', e.target.value)} title="From date" />
        <input type="date" value={filters.toDate} onChange={(e) => setFilter('toDate', e.target.value)} title="To date" />
        <button className="pt-clear" onClick={() => { setFilters(EMPTY_FILTERS); setQuickView('all'); }}>Clear</button>
      </div>

      {/* Body */}
      {error ? (
        <div className="job-card-error"><AlertTriangle size={14} /> {error}</div>
      ) : loading && trades.length === 0 ? (
        <p className="job-empty">Loading paper trades…</p>
      ) : filtered.length === 0 ? (
        <p className="job-empty">No paper trades match the current filters.</p>
      ) : (
        <div className="pt-table-wrap">
          <table className="pt-table">
            <thead>
              <tr>
                <th>ID</th><th>TF</th><th>Pattern</th><th>Type</th><th>Status</th>
                <th>Candle Time</th><th>Entry</th><th>SL</th>
                <th>TP1</th><th>TP2</th><th>TP3</th><th>TP4</th>
                <th>Safe</th><th>TP Progress</th><th>Conf</th><th>Closed</th><th>Created</th><th></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((t) => (
                <tr key={t.tradeId}>
                  <td>{t.tradeId}</td>
                  <td><span className="tf-badge">{t.timeframe}</span></td>
                  <td className="pt-pattern">{prettyPattern(t.patternName)}</td>
                  <td>
                    <span className={`signal-badge ${t.tradeType === 'BULLISH' ? 'sig-bull' : 'sig-bear'}`}>
                      {t.tradeType === 'BULLISH' ? 'BULL' : 'BEAR'}
                    </span>
                  </td>
                  <td>
                    <span className={`pt-status pt-status--${t.tradeStatus.toLowerCase()}`}>{t.tradeStatus}</span>
                  </td>
                  <td className="pt-time">{fmt(t.candleTime)}</td>
                  <td>{num(t.tradePrice)}</td>
                  <td>{num(t.stopLoss)}</td>
                  <td>{num(t.tp1)}</td><td>{num(t.tp2)}</td><td>{num(t.tp3)}</td><td>{num(t.tp4)}</td>
                  <td>
                    <span className={`pt-safe${t.safeTrade ? ' on' : ''}`}>
                      {t.safeTrade ? <Shield size={12} /> : null}{t.safeTrade ? 'Safe' : '—'}
                    </span>
                  </td>
                  <td><TpProgress t={t} /></td>
                  <td>{t.confidenceScore != null ? `${Math.round(t.confidenceScore * 100)}%` : '—'}</td>
                  <td>{fmt(t.closeReason)}</td>
                  <td className="pt-time">{fmt(t.createdAt)}</td>
                  <td>
                    <button className="pt-view-btn" onClick={() => setOpenTrade(t)}>View</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Detail modal */}
      {openTrade && (
        <div className="modal-overlay" onClick={() => setOpenTrade(null)} role="dialog" aria-modal="true">
          <div className="modal-box modal-box--wide" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>
                #{openTrade.tradeId} {prettyPattern(openTrade.patternName)}{' '}
                <span className="tf-badge">{openTrade.timeframe}</span>{' '}
                <span className={`signal-badge ${openTrade.tradeType === 'BULLISH' ? 'sig-bull' : 'sig-bear'}`}>
                  {openTrade.tradeType === 'BULLISH'
                    ? <TrendingUp size={12} /> : <TrendingDown size={12} />} {openTrade.tradeType}
                </span>
              </h3>
              <button className="modal-close-btn" onClick={() => setOpenTrade(null)} aria-label="Close">
                <X size={16} />
              </button>
            </div>
            <div className="modal-body modal-body--scroll">
              <div className="job-modal-section">
                <h4 className="job-modal-section-title">Trade Plan</h4>
                <div className="job-modal-grid">
                  <Field label="Status" value={openTrade.tradeStatus} />
                  <Field label="Entry" value={num(openTrade.tradePrice)} />
                  <Field label="Stop Loss" value={num(openTrade.stopLoss)} />
                  <Field label="Initial SL" value={num(openTrade.initialStopLoss)} />
                  <Field label="TP1" value={num(openTrade.tp1)} />
                  <Field label="TP2" value={num(openTrade.tp2)} />
                  <Field label="TP3" value={num(openTrade.tp3)} />
                  <Field label="TP4" value={num(openTrade.tp4)} />
                  <Field label="Risk / unit" value={num(openTrade.riskAmount)} />
                  <Field label="Breakout" value={num(openTrade.breakoutLevel)} />
                  <Field label="ATR" value={num(openTrade.atrAtDetection)} />
                  <Field label="Confidence" value={openTrade.confidenceScore != null ? `${Math.round(openTrade.confidenceScore * 100)}%` : '—'} />
                  <Field label="Safe trade" value={openTrade.safeTrade ? 'Yes' : 'No'} />
                  <Field label="Close reason" value={fmt(openTrade.closeReason)} />
                  <Field label="Candle time" value={fmt(openTrade.candleTime)} />
                  <Field label="Opened" value={fmt(openTrade.openedAt)} />
                  <Field label="Closed" value={fmt(openTrade.closedAt)} />
                  <Field label="Last eval" value={fmt(openTrade.lastEvaluatedAt)} />
                </div>
                {openTrade.detectionReason ? (
                  <div className="pt-reason">{openTrade.detectionReason}</div>
                ) : null}
              </div>

              <div className="job-modal-section">
                <h4 className="job-modal-section-title">Crossover Snapshot (at creation)</h4>
                <div className="job-modal-grid">
                  <Field label="1m" value={fmt(openTrade.oneMinuteCrossoverState)} />
                  <Field label="5m" value={fmt(openTrade.fiveMinuteCrossoverState)} />
                  <Field label="15m" value={fmt(openTrade.fifteenMinuteCrossoverState)} />
                  <Field label="1h" value={fmt(openTrade.oneHourCrossoverState)} />
                </div>
              </div>

              {openTrade.paperCandle && (
                <div className="job-modal-section">
                  <h4 className="job-modal-section-title">Signal Candle (OHLCV)</h4>
                  <div className="job-modal-grid">
                    <Field label="Open" value={num(openTrade.paperCandle.openPrice)} />
                    <Field label="High" value={num(openTrade.paperCandle.highPrice)} />
                    <Field label="Low" value={num(openTrade.paperCandle.lowPrice)} />
                    <Field label="Close" value={num(openTrade.paperCandle.closePrice)} />
                    <Field label="Volume" value={num(openTrade.paperCandle.volume)} />
                    <Field label="Candle time" value={fmt(openTrade.paperCandle.candleTime)} />
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const Field: React.FC<{ label: string; value: React.ReactNode }> = ({ label, value }) => (
  <div className="modal-field">
    <span className="modal-field-label">{label}</span>
    <span className="modal-field-value">{value}</span>
  </div>
);

export default PaperTradingPage;
