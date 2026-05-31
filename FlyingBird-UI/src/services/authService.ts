import api from './api';
import type { LoginRequestDto, AuthResponseDto } from '../types/auth';

export const login = async (credentials: LoginRequestDto): Promise<AuthResponseDto> => {
  const response = await api.post<AuthResponseDto>('/api/auth/login', credentials);
  return response.data;
};

export const isTokenValid = (): boolean => {
  const token = localStorage.getItem('token');
  if (!token) return false;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
};

export const logout = (): void => {
  localStorage.removeItem('token');
};
