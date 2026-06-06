import api from './api';
import type {
  AdminRegisterUserRequestDto,
  AdminDisableUserRequestDto,
  AuthResponseDto,
} from '../types/auth';

/**
 * Admin-only: register a new user.
 * Uses the central `api` instance, so the JWT is attached by the request interceptor —
 * no manual token handling here. Backend enforces ADMIN via @PreAuthorize.
 */
export const registerUserByAdmin = async (
  payload: AdminRegisterUserRequestDto,
): Promise<AuthResponseDto> => {
  const response = await api.post<AuthResponseDto>('/api/admin/users/register', payload);
  return response.data;
};

/**
 * Admin-only: disable a user (sets enabled=false). Backend rejects disabling ADMINs (403)
 * and unknown usernames (404). JWT attached by the central interceptor.
 */
export const disableUserByAdmin = async (
  payload: AdminDisableUserRequestDto,
): Promise<AuthResponseDto> => {
  const response = await api.post<AuthResponseDto>('/api/admin/users/disable', payload);
  return response.data;
};
