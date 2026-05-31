import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { isTokenValid } from '../services/authService';

interface Props {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<Props> = ({ children }) => {
  const { isAuthenticated, logout } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isTokenValid()) {
    logout();
    const msg = encodeURIComponent('Session expired. Please login again.');
    return <Navigate to={`/login?error=${msg}`} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
