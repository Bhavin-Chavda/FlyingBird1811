import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { UserDetailsResponseDto } from '../types/auth';
import {
  isTokenValid,
  logout as authLogout,
  getUserDetails,
  getUsernameFromToken,
} from '../services/authService';
import { markManualLogout } from '../services/api';

interface AuthContextType {
  userDetails: UserDetailsResponseDto | null;
  isAuthenticated: boolean;
  setAuth: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [userDetails, setUserDetails]       = useState<UserDetailsResponseDto | null>(null);
  // Initialise from localStorage so page-refresh keeps the user authenticated.
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => isTokenValid());

  // On page load/refresh: if a valid token exists, fetch user details in the background.
  useEffect(() => {
    if (!isTokenValid()) return;
    const username = getUsernameFromToken();
    if (username) {
      getUserDetails(username)
        .then(details => setUserDetails(details))
        .catch(() => {
          authLogout();
          setIsAuthenticated(false);
        });
    } else {
      authLogout();
      setIsAuthenticated(false);
    }
  }, []);

  // Store token and mark authenticated; fetch user details in the background.
  // Caller (LoginPage) can navigate immediately without waiting for the details API.
  const setAuth = useCallback((token: string) => {
    localStorage.setItem('token', token);
    setIsAuthenticated(true);
    const username = getUsernameFromToken();
    if (username) {
      getUserDetails(username)
        .then(details => setUserDetails(details))
        .catch(() => { /* token is valid; details will populate on next attempt */ });
    }
  }, []);

  const logout = useCallback(() => {
    markManualLogout();
    authLogout();
    setUserDetails(null);
    setIsAuthenticated(false);
  }, []);

  return (
    <AuthContext.Provider value={{ userDetails, isAuthenticated, setAuth, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
