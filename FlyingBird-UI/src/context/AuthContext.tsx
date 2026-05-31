import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { User } from '../types/auth';
import { isTokenValid, logout as authLogout } from '../services/authService';
import { markManualLogout } from '../services/api';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  setAuth: (token: string, user: User) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    if (isTokenValid()) {
      const storedUsername = localStorage.getItem('userUsername');
      const storedRole     = localStorage.getItem('userRole');
      if (storedUsername) {
        setUser({ username: storedUsername, role: storedRole || '' });
      }
    } else {
      authLogout();
      localStorage.removeItem('userUsername');
      localStorage.removeItem('userRole');
    }
  }, []);

  const setAuth = useCallback((token: string, userData: User) => {
    localStorage.setItem('token',       token);
    localStorage.setItem('userUsername', userData.username);
    localStorage.setItem('userRole',     userData.role);
    setUser(userData);
  }, []);

  const logout = useCallback(() => {
    markManualLogout();
    authLogout();
    localStorage.removeItem('userUsername');
    localStorage.removeItem('userRole');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, setAuth, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
