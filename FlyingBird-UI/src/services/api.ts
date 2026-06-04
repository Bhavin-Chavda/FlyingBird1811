import axios from 'axios';
import { BASE_URL } from '../config';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Set before clearing the token so in-flight 401s are silently dropped.
let _manualLogout = false;
export const markManualLogout = () => { _manualLogout = true; };

// Decode a JWT's `exp` and check it is still in the future. Inlined here (not
// imported from authService) to avoid a circular import (authService → api).
const isStoredTokenValid = (token: string | null): boolean => {
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return typeof payload.exp === 'number' && payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  // Never send a stale/expired token — purge it so it can't trigger a 401 loop.
  if (token && !isStoredTokenValid(token)) {
    localStorage.removeItem('token');
    return config;
  }
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isLoginRequest = error.config?.url?.includes('/auth/login');
    if (error.response?.status === 401 && !isLoginRequest && !_manualLogout) {
      // Only end the session if the CURRENT stored token is itself invalid.
      // A 401 while a valid token is stored means a stale in-flight request
      // failed — don't clobber the freshly-established session.
      if (!isStoredTokenValid(localStorage.getItem('token'))) {
        localStorage.removeItem('token');
        const msg = error.response?.data?.message || 'Session expired. Please login again.';
        window.location.href = `/login?error=${encodeURIComponent(msg)}`;
      }
    }
    return Promise.reject(error);
  }
);

export default api;
