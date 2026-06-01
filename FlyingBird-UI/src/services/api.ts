import axios from 'axios';
import { BASE_URL } from '../config';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Set before clearing the token so in-flight 401s are silently dropped.
let _manualLogout = false;
export const markManualLogout = () => { _manualLogout = true; };

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
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
      localStorage.removeItem('token');
      const msg = error.response?.data?.message || 'Session expired. Please login again.';
      window.location.href = `/login?error=${encodeURIComponent(msg)}`;
    }
    return Promise.reject(error);
  }
);

export default api;
