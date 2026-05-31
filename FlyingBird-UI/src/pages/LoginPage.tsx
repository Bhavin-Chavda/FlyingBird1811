import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Lock, AlertCircle, User } from 'lucide-react';
import { login } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import type { ErrorResponseDto } from '../types/auth';
import FlyingBirdLogo from '../components/FlyingBirdLogo';

const TICKER_ITEMS = [
  { symbol: 'BTC',   value: '67,420.50', change: '+2.34%', up: true  },
  { symbol: 'ETH',   value: '3,521.80',  change: '+1.87%', up: true  },
  { symbol: 'GOLD',  value: '2,318.45',  change: '-0.12%', up: false },
  { symbol: 'S&P',   value: '5,248.80',  change: '+0.76%', up: true  },
  { symbol: 'AAPL',  value: '189.30',    change: '+1.20%', up: true  },
  { symbol: 'TSLA',  value: '172.63',    change: '-0.85%', up: false },
  { symbol: 'NIFTY', value: '22,147.00', change: '+0.54%', up: true  },
  { symbol: 'XAU',   value: '2,318.45',  change: '-0.12%', up: false },
];

const LoginPage: React.FC = () => {
  const navigate       = useNavigate();
  const [searchParams] = useSearchParams();
  const { setAuth, isAuthenticated } = useAuth();
  const { showSuccess, showError }   = useToast();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  useEffect(() => {
    if (isAuthenticated) navigate('/dashboard', { replace: true });
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    const urlError = searchParams.get('error');
    if (urlError) {
      setError(urlError);
      showError(urlError);
    }
  }, [searchParams, showError]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!username.trim()) { setError('Username is required.'); return; }
    if (!password.trim()) { setError('Password is required.'); return; }

    setLoading(true);
    try {
      const response = await login({ username, password });
      setAuth(response.token, { username: response.username, role: response.role });
      showSuccess(`${response.username} logged in successfully`);
      navigate('/dashboard', { replace: true });
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: ErrorResponseDto; status?: number } };
      const message  = axiosErr.response?.data?.message || 'Unable to connect. Please check your network.';
      setError(message);
      showError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-root">

      {/* Main Layout */}
      <div className="login-layout">
        {/* Left Panel */}
        <div className="login-left">
          <div className="brand">
            <FlyingBirdLogo size={56} className="brand-icon" animated />
            <h1 className="brand-name">FlyingBird</h1>
            <p className="brand-tagline">Market Intelligence Platform</p>
          </div>

          <div className="market-symbols">
            <div className="symbol-grid">
              {['₿', '◈', 'Au', '📈', '$', '¥', '€', '£'].map((s, i) => (
                <span key={i} className={`mkt-symbol s${i}`}>{s}</span>
              ))}
            </div>
          </div>

          <div className="stats-row">
            {TICKER_ITEMS.slice(0, 4).map((item) => (
              <div key={item.symbol} className="stat-card">
                <span className="stat-sym">{item.symbol}</span>
                <span className={`stat-chg ${item.up ? 'up' : 'down'}`}>{item.change}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Right Panel — Login Form */}
        <div className="login-right">
          <div className="login-card">
            <div className="login-header">
              <FlyingBirdLogo size={36} className="card-icon" animated />
              <h2>Sign In</h2>
              <p>Access your market dashboard</p>
            </div>

            <form onSubmit={handleSubmit} noValidate>
              <div className="field-group">
                <label htmlFor="username">Username</label>
                <div className="input-wrap">
                  <User size={16} className="input-icon" />
                  <input
                    id="username"
                    type="text"
                    placeholder="Enter your username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    autoComplete="username"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className="field-group">
                <label htmlFor="password">Password</label>
                <div className="input-wrap">
                  <Lock size={16} className="input-icon" />
                  <input
                    id="password"
                    type="password"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="current-password"
                    disabled={loading}
                  />
                </div>
              </div>

              {error && (
                <div className="error-msg">
                  <AlertCircle size={14} />
                  <span>{error}</span>
                </div>
              )}

              <button type="submit" className="btn-login" disabled={loading}>
                {loading ? (
                  <span className="spinner-wrap">
                    <span className="spinner" />
                    Authenticating...
                  </span>
                ) : (
                  'Sign In'
                )}
              </button>
            </form>

            <p className="login-footer">
              Secured with JWT · All data encrypted in transit
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
