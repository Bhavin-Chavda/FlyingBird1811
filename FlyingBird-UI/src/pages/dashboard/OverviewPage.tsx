import React from 'react';
import {
  TrendingUp, TrendingDown, Activity, DollarSign,
  BarChart2, Bitcoin, Gem,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const MARKET_DATA = [
  { symbol: 'BTC/USD', name: 'Bitcoin',   value: '67,420.50', change: '+2.34%', up: true,  icon: '₿'  },
  { symbol: 'ETH/USD', name: 'Ethereum',  value: '3,521.80',  change: '+1.87%', up: true,  icon: '◈'  },
  { symbol: 'XAU/USD', name: 'Gold',      value: '2,318.45',  change: '-0.12%', up: false, icon: 'Au' },
  { symbol: 'S&P 500', name: 'Index',     value: '5,248.80',  change: '+0.76%', up: true,  icon: '📊' },
  { symbol: 'AAPL',    name: 'Apple Inc.',value: '189.30',    change: '+1.20%', up: true,  icon: '◉'  },
  { symbol: 'TSLA',    name: 'Tesla Inc.',value: '172.63',    change: '-0.85%', up: false, icon: '◎'  },
];

const STATS = [
  { label: 'Portfolio Value',  value: '$1,24,580', icon: DollarSign, up: true,  delta: '+3.2%'  },
  { label: 'Active Positions', value: '12',        icon: Activity,   up: true,  delta: '+2'     },
  { label: 'Day P&L',          value: '+$1,240',   icon: TrendingUp, up: true,  delta: '+0.99%' },
  { label: 'Watchlist',        value: '28',        icon: BarChart2,  up: false, delta: 'assets' },
];

const OverviewPage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div className="page-content">
      {/* Welcome */}
      <div className="welcome-row">
        <div>
          <h2 className="welcome-title">
            Welcome back, <span className="accent">{user?.username}</span>
          </h2>
          <p className="welcome-sub">Here's your market snapshot for today.</p>
        </div>
        <div className="live-badge">
          <span className="pulse-dot" />
          Live
        </div>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        {STATS.map((s) => (
          <div className="stat-tile" key={s.label}>
            <div className="stat-tile-top">
              <span className="stat-tile-label">{s.label}</span>
              <s.icon size={18} className={`stat-tile-icon ${s.up ? 'up' : 'down'}`} />
            </div>
            <div className="stat-tile-value">{s.value}</div>
            <div className={`stat-tile-delta ${s.up ? 'up' : 'neutral'}`}>{s.delta}</div>
          </div>
        ))}
      </div>

      {/* Market Table */}
      <div className="section">
        <div className="section-header">
          <h3>
            <BarChart2 size={16} className="section-icon" />
            Market Overview
          </h3>
        </div>
        <div className="market-table-wrap">
          <table className="market-table">
            <thead>
              <tr>
                <th>Asset</th>
                <th>Symbol</th>
                <th>Price (USD)</th>
                <th>24h Change</th>
                <th>Trend</th>
              </tr>
            </thead>
            <tbody>
              {MARKET_DATA.map((row) => (
                <tr key={row.symbol}>
                  <td>
                    <span className="asset-icon">{row.icon}</span>
                    {row.name}
                  </td>
                  <td className="symbol-cell">{row.symbol}</td>
                  <td className="value-cell">{row.value}</td>
                  <td className={row.up ? 'up' : 'down'}>{row.change}</td>
                  <td>
                    {row.up
                      ? <TrendingUp size={16} className="up" />
                      : <TrendingDown size={16} className="down" />}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Asset Class Cards */}
      <div className="asset-cards">
        <div className="asset-card crypto">
          <Bitcoin size={32} className="asset-card-icon" />
          <div>
            <h4>Crypto</h4>
            <p>BTC · ETH · XRP · SOL</p>
          </div>
        </div>
        <div className="asset-card commodities">
          <Gem size={32} className="asset-card-icon" />
          <div>
            <h4>Commodities</h4>
            <p>Gold · Silver · Oil · Platinum</p>
          </div>
        </div>
        <div className="asset-card equities">
          <BarChart2 size={32} className="asset-card-icon" />
          <div>
            <h4>Equities</h4>
            <p>NYSE · NASDAQ · NSE · BSE</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OverviewPage;
