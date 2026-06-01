import React, { useState, useCallback } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  Briefcase,
  ArrowLeftRight,
  History,
  BarChart2,
  LogOut,
  ChevronLeft,
  ChevronRight,
  User,
  LayoutDashboard,
  Menu,
  X,
  Shield,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import FlyingBirdLogo from '../components/FlyingBirdLogo';

const NAV_ITEMS = [
  { label: 'Overview',     path: '/dashboard',              icon: LayoutDashboard, end: true },
  { label: 'Jobs Details', path: '/dashboard/jobs-details', icon: Briefcase },
  { label: 'Trades',       path: '/dashboard/trades',       icon: ArrowLeftRight },
  { label: 'History',      path: '/dashboard/history',      icon: History },
  { label: 'Analytics',    path: '/dashboard/analytics',    icon: BarChart2 },
];

const DashboardPage: React.FC = () => {
  const { userDetails, logout } = useAuth();
  const { showSuccess } = useToast();
  const navigate = useNavigate();

  const [collapsed,     setCollapsed]     = useState(false);
  const [mobileOpen,    setMobileOpen]    = useState(false);
  const [userModalOpen, setUserModalOpen] = useState(false);

  const closeMobile = useCallback(() => setMobileOpen(false), []);

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
        ].join(' ')}
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
          {!collapsed && <span className="sb-section-label">Menu</span>}
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
        </nav>

        {/* Footer */}
        <div className="sb-footer">
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
      </aside>

      {/* Main content */}
      <div className="db-body">
        {/* Top bar — sidebar toggle at top-left */}
        <div className="db-topbar">
          {/* Desktop toggle */}
          <button
            className="db-topbar-btn db-topbar-btn--desktop"
            onClick={() => setCollapsed((c) => !c)}
            title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
          </button>
          {/* Mobile hamburger */}
          <button
            className="db-topbar-btn db-topbar-btn--mobile"
            onClick={() => setMobileOpen(true)}
            title="Open menu"
          >
            <Menu size={18} />
          </button>
          {/* Mobile brand label */}
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
    </div>
  );
};

export default DashboardPage;
