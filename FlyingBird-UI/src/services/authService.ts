import api from './api';
import type { LoginRequestDto, AuthResponseDto, UserDetailsResponseDto } from '../types/auth';

export const login = async (credentials: LoginRequestDto): Promise<AuthResponseDto> => {
  const response = await api.post<AuthResponseDto>('/api/auth/login', credentials);
  return response.data;
};

export const getUserDetails = async (username: string): Promise<UserDetailsResponseDto> => {
  const response = await api.post<UserDetailsResponseDto>('/api/users/userDetails', { username });
  return response.data;
};

export const getUsernameFromToken = (): string | null => {
  const token = localStorage.getItem('token');
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub ?? null;
  } catch {
    return null;
  }
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
