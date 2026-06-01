import React from 'react';
import { TrendingUp, LogOut, User } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar: React.FC = () => {
  const { userDetails, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <TrendingUp size={22} className="nav-icon" />
        <span>FlyingBird</span>
      </div>
      <div className="navbar-right">
        <div className="nav-user">
          <User size={14} />
          <span>{userDetails?.username}</span>
        </div>
        <button className="btn-logout" onClick={handleLogout} title="Logout">
          <LogOut size={16} />
          <span>Logout</span>
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
