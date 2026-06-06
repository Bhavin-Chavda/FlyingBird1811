import React, { useState, useCallback } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  Briefcase,
  ArrowLeftRight,
  History,
  BarChart2,
  CandlestickChart,
  LogOut,
  ChevronLeft,
  ChevronRight,
  User,
  UserPlus,
  UserX,
  Lock,
  AlertCircle,
  LayoutDashboard,
  Menu,
  X,
  Shield,
  Sparkles,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { registerUserByAdmin, disableUserByAdmin } from '../services/adminService';
import type { ErrorResponseDto } from '../types/auth';
import FlyingBirdLogo from '../components/FlyingBirdLogo';

const NAV_ITEMS = [
  { label: 'Overview',     path: '/dashboard',              icon: LayoutDashboard, end: true },
  { label: 'Jobs Details', path: '/dashboard/jobs-details', icon: Briefcase },
  { label: 'Paper Trading', path: '/dashboard/paper-trading', icon: CandlestickChart },
  { label: 'Trades',       path: '/dashboard/trades',       icon: ArrowLeftRight },
  { label: 'History',      path: '/dashboard/history',      icon: History },
  { label: 'Analytics',    path: '/dashboard/analytics',    icon: BarChart2 },
];

// Sidebar drag-resize bounds (px). Min keeps it usable; max keeps layout intact.
const MIN_SB_W = 72;
const MAX_SB_W = 320;
const DEFAULT_SB_W = 220;

const DashboardPage: React.FC = () => {
  const { userDetails, logout } = useAuth();
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();

  const [collapsed,     setCollapsed]     = useState(false);
  const [mobileOpen,    setMobileOpen]    = useState(false);
  const [userModalOpen, setUserModalOpen] = useState(false);
  const [sidebarWidth,  setSidebarWidth]  = useState(DEFAULT_SB_W);
  const [resizing,      setResizing]      = useState(false);

  // Admin Access modal — two tabs: register a user / disable a user.
  const isAdmin = userDetails?.role === 'ADMIN';
  const [adminModalOpen, setAdminModalOpen] = useState(false);
  const [adminTab, setAdminTab] = useState<'register' | 'disable' | 'affirmations'>('register');
  // Register-user form
  const [regUsername, setRegUsername] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regRole,     setRegRole]     = useState('USER');
  const [regError,    setRegError]    = useState('');
  const [regSubmitting, setRegSubmitting] = useState(false);
  // Disable-user form
  const [disUsername,   setDisUsername]   = useState('');
  const [disError,      setDisError]      = useState('');
  const [disSubmitting, setDisSubmitting] = useState(false);

  const closeMobile = useCallback(() => setMobileOpen(false), []);

  const openAdminModal = () => {
    setAdminTab('register');
    setRegUsername('');
    setRegPassword('');
    setRegRole('USER');
    setRegError('');
    setDisUsername('');
    setDisError('');
    setAdminModalOpen(true);
  };

  const handleRegisterUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setRegError('');

    if (!regUsername.trim()) { setRegError('Username is required.'); return; }
    if (!regPassword.trim()) { setRegError('Password is required.'); return; }

    setRegSubmitting(true);
    try {
      const res = await registerUserByAdmin({
        username: regUsername.trim(),
        password: regPassword,
        role: regRole,
      });
      showSuccess(res.message || `User "${res.username}" registered successfully`);
      setAdminModalOpen(false);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: ErrorResponseDto; status?: number } };
      const message = axiosErr.response?.data?.message || 'Unable to register user. Please try again.';
      setRegError(message);
      showError(message);
    } finally {
      setRegSubmitting(false);
    }
  };

  const handleDisableUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setDisError('');

    if (!disUsername.trim()) { setDisError('Username is required.'); return; }

    setDisSubmitting(true);
    try {
      const res = await disableUserByAdmin({ username: disUsername.trim() });
      showSuccess(res.message || `User "${res.username}" disabled`);
      setAdminModalOpen(false);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: ErrorResponseDto; status?: number } };
      const message = axiosErr.response?.data?.message || 'Unable to disable user. Please try again.';
      setDisError(message);
      showError(message);
    } finally {
      setDisSubmitting(false);
    }
  };

  // Drag the sidebar's right edge to resize (desktop only — the handle is hidden
  // on mobile and the overlay sidebar uses a fixed width there).
  const startResize = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    setResizing(true);
    document.body.style.userSelect = 'none';
    document.body.style.cursor = 'ew-resize';

    const onMove = (ev: MouseEvent) => {
      const next = Math.min(MAX_SB_W, Math.max(MIN_SB_W, ev.clientX));
      setSidebarWidth(next);
    };
    const onUp = () => {
      setResizing(false);
      document.body.style.userSelect = '';
      document.body.style.cursor = '';
      window.removeEventListener('mousemove', onMove);
      window.removeEventListener('mouseup', onUp);
    };
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
  }, []);

  const handleLogout = () => {
    logout();
    showSuccess('Logged out successfully');
    navigate('/login', { replace: true });
  };

  return (
    <div className="db-shell">
      {/* Mobile backdrop */}
      {mobileOpen && (
        <div className="sb-backdrop" onClick={closeMobile} aria-hidden="true" />
      )}

      {/* Sidebar */}
      <aside
        className={[
          'sidebar',
          collapsed  ? 'sidebar--collapsed'   : '',
          mobileOpen ? 'sidebar--mobile-open' : '',
          resizing   ? 'sidebar--resizing'    : '',
        ].join(' ')}
        style={collapsed ? undefined : { width: sidebarWidth, minWidth: sidebarWidth }}
      >
        {/* Brand */}
        <div className="sb-brand">
          <FlyingBirdLogo size={26} className="sb-brand-icon" />
          {!collapsed && <span className="sb-brand-name">FlyingBird</span>}
          <button className="sb-mobile-close" onClick={closeMobile} title="Close menu">
            <X size={16} />
          </button>
        </div>

        {/* Nav */}
        <nav className="sb-nav">
          {NAV_ITEMS.map(({ label, path, icon: Icon, end }) => (
            <NavLink
              key={path}
              to={path}
              end={end}
              onClick={closeMobile}
              className={({ isActive }) =>
                `sb-nav-item${isActive ? ' sb-nav-item--active' : ''}`
              }
              title={collapsed ? label : undefined}
            >
              <Icon size={18} className="sb-nav-icon" />
              {!collapsed && <span className="sb-nav-label">{label}</span>}
            </NavLink>
          ))}

          {/* Collapse/minimize toggle — sits below the nav items */}
          <button
            className="sb-nav-item sb-collapse-btn"
            onClick={() => setCollapsed((c) => !c)}
            title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed
              ? <ChevronRight size={18} className="sb-nav-icon" />
              : <ChevronLeft size={18} className="sb-nav-icon" />}
            {!collapsed && <span className="sb-nav-label">Collapse</span>}
          </button>
        </nav>

        {/* Footer */}
        <div className="sb-footer">
          {/* Admin Access — always visible; disabled + greyed for non-admins. */}
          <button
            className={`sb-user-btn${collapsed ? ' sb-user-btn--collapsed' : ''}`}
            onClick={openAdminModal}
            disabled={!isAdmin}
            title={isAdmin ? 'Register a new user' : 'Admin access required'}
          >
            <div className="sb-user-avatar">
              <Shield size={14} />
            </div>
            {!collapsed && (
              <div className="sb-user-info">
                <span className="sb-user-name">Admin Access</span>
                {!isAdmin && <span className="sb-user-sub">Admin access required</span>}
              </div>
            )}
          </button>

          <button
            className={`sb-user-btn${collapsed ? ' sb-user-btn--collapsed' : ''}`}
            onClick={() => setUserModalOpen(true)}
            title="View user details"
          >
            <div className="sb-user-avatar">
              <User size={14} />
            </div>
            {!collapsed && (
              <div className="sb-user-info">
                <span className="sb-user-name">{userDetails?.username}</span>
              </div>
            )}
          </button>
          <button className="sb-logout" onClick={handleLogout} title="Logout">
            <LogOut size={16} />
            {!collapsed && <span>Logout</span>}
          </button>
        </div>

        {/* Drag handle — resize the sidebar (hidden when collapsed / on mobile) */}
        {!collapsed && (
          <div
            className="sb-resize-handle"
            onMouseDown={startResize}
            role="separator"
            aria-orientation="vertical"
            aria-label="Resize sidebar"
            title="Drag to resize"
          />
        )}
      </aside>

      {/* Main content */}
      <div className="db-body">
        {/* Top bar — mobile only: hamburger opens the sidebar drawer. Hidden on
            desktop (the sidebar is the sole navigation; collapse lives in it). */}
        <div className="db-topbar">
          <button
            className="db-topbar-btn db-topbar-btn--mobile"
            onClick={() => setMobileOpen(true)}
            title="Open menu"
          >
            <Menu size={18} />
          </button>
          <div className="db-topbar-brand">
            <FlyingBirdLogo size={20} />
            <span>FlyingBird</span>
          </div>
        </div>

        <Outlet />
      </div>

      {/* User details modal */}
      {userModalOpen && (
        <div
          className="modal-overlay"
          onClick={() => setUserModalOpen(false)}
          role="dialog"
          aria-modal="true"
          aria-label="User Details"
        >
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>User Details</h3>
              <button
                className="modal-close-btn"
                onClick={() => setUserModalOpen(false)}
                aria-label="Close"
              >
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <div className="modal-avatar">
                <User size={32} />
              </div>
              <div className="modal-field">
                <span className="modal-field-label">ID</span>
                <span className="modal-field-value">{userDetails?.id ?? '—'}</span>
              </div>
              <div className="modal-field">
                <span className="modal-field-label">Username</span>
                <span className="modal-field-value">{userDetails?.username || '—'}</span>
              </div>
              <div className="modal-field">
                <span className="modal-field-label">Role</span>
                <span className="modal-field-value">
                  <span className="modal-role-badge">
                    <Shield size={12} />
                    {userDetails?.role || '—'}
                  </span>
                </span>
              </div>
              <div className="modal-field">
                <span className="modal-field-label">Status</span>
                <span className="modal-field-value">{userDetails?.enabled ? 'Active' : 'Inactive'}</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Admin Access modal — Register User (ADMIN only; button is disabled otherwise) */}
      {adminModalOpen && isAdmin && (
        <div
          className="modal-overlay"
          onClick={() => setAdminModalOpen(false)}
          role="dialog"
          aria-modal="true"
          aria-label="Admin Access - Register User"
        >
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Admin Access</h3>
              <button
                className="modal-close-btn"
                onClick={() => setAdminModalOpen(false)}
                aria-label="Close"
              >
                <X size={18} />
              </button>
            </div>
            <div className="modal-body modal-body--form">
              {/* Tabs */}
              <div className="admin-tabs" role="tablist">
                <button
                  type="button"
                  role="tab"
                  aria-selected={adminTab === 'register'}
                  className={`admin-tab${adminTab === 'register' ? ' admin-tab--active' : ''}`}
                  onClick={() => setAdminTab('register')}
                >
                  <UserPlus size={14} />
                  Register User
                </button>
                <button
                  type="button"
                  role="tab"
                  aria-selected={adminTab === 'disable'}
                  className={`admin-tab${adminTab === 'disable' ? ' admin-tab--active' : ''}`}
                  onClick={() => setAdminTab('disable')}
                >
                  <UserX size={14} />
                  Disable User
                </button>
                <button
                  type="button"
                  role="tab"
                  aria-selected={adminTab === 'affirmations'}
                  className={`admin-tab${adminTab === 'affirmations' ? ' admin-tab--active' : ''}`}
                  onClick={() => setAdminTab('affirmations')}
                >
                  <Sparkles size={14} />
                  Affirmations
                </button>
              </div>

              {adminTab === 'register' && (
              <form className="admin-form" onSubmit={handleRegisterUser} noValidate>
                <div className="field-group">
                  <label htmlFor="reg-username">Username</label>
                  <div className="input-wrap">
                    <User size={16} className="input-icon" />
                    <input
                      id="reg-username"
                      type="text"
                      placeholder="Enter username"
                      value={regUsername}
                      onChange={(e) => setRegUsername(e.target.value)}
                      autoComplete="off"
                      disabled={regSubmitting}
                    />
                  </div>
                </div>

                <div className="field-group">
                  <label htmlFor="reg-password">Password</label>
                  <div className="input-wrap">
                    <Lock size={16} className="input-icon" />
                    <input
                      id="reg-password"
                      type="password"
                      placeholder="••••••••"
                      value={regPassword}
                      onChange={(e) => setRegPassword(e.target.value)}
                      autoComplete="new-password"
                      disabled={regSubmitting}
                    />
                  </div>
                </div>

                <div className="field-group">
                  <label htmlFor="reg-role">Role</label>
                  <div className="input-wrap">
                    <Shield size={16} className="input-icon" />
                    <select
                      id="reg-role"
                      value={regRole}
                      onChange={(e) => setRegRole(e.target.value)}
                      disabled={regSubmitting}
                    >
                      <option value="USER">USER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </div>
                </div>

                {regError && (
                  <div className="error-msg">
                    <AlertCircle size={14} />
                    <span>{regError}</span>
                  </div>
                )}

                <button type="submit" className="btn-login" disabled={regSubmitting}>
                  {regSubmitting ? (
                    <span className="spinner-wrap">
                      <span className="spinner" />
                      Registering...
                    </span>
                  ) : (
                    <span className="spinner-wrap">
                      <UserPlus size={16} />
                      Register User
                    </span>
                  )}
                </button>
              </form>
              )}

              {adminTab === 'disable' && (
              <form className="admin-form" onSubmit={handleDisableUser} noValidate>
                <p className="admin-form-note">
                  Disables a user (sets them inactive) so they can no longer log in.
                  ADMIN users cannot be disabled.
                </p>
                <div className="field-group">
                  <label htmlFor="dis-username">Username</label>
                  <div className="input-wrap">
                    <User size={16} className="input-icon" />
                    <input
                      id="dis-username"
                      type="text"
                      placeholder="Enter username to disable"
                      value={disUsername}
                      onChange={(e) => setDisUsername(e.target.value)}
                      autoComplete="off"
                      disabled={disSubmitting}
                    />
                  </div>
                </div>

                {disError && (
                  <div className="error-msg">
                    <AlertCircle size={14} />
                    <span>{disError}</span>
                  </div>
                )}

                <button type="submit" className="btn-login btn-danger" disabled={disSubmitting}>
                  {disSubmitting ? (
                    <span className="spinner-wrap">
                      <span className="spinner" />
                      Disabling...
                    </span>
                  ) : (
                    <span className="spinner-wrap">
                      <UserX size={16} />
                      Disable User
                    </span>
                  )}
                </button>
              </form>
              )}

              {adminTab === 'affirmations' && (
              <div className="admin-affirmation">
                <Sparkles size={22} className="admin-affirmation-icon" />
                <p>
                  Dear Universe, I'm ready and open for you to start conspiring in my favor,
                  and I request a clear sign in my reality to make my dreams come true.
                  I'm ready and open.
                </p>
                <p className="admin-affirmation-goal">Ultimate Goal: 4273350814</p>
                <p>
                  Dear God, you will be proud of me for choosing me for this vision —
                  and that's a promise.
                </p>
              </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
