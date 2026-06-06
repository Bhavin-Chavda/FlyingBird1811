export interface LoginRequestDto {
  username: string;
  password: string;
}

export interface AuthResponseDto {
  token: string;
  username: string;
  role: string;
  message: string;
}

export interface ErrorResponseDto {
  statusCode: number;
  error: string;
  message: string;
  timestamp: string;
  errorCode: string;
}

export interface UserDetailsRequestDto {
  username: string;
}

export interface UserDetailsResponseDto {
  id: number;
  username: string;
  role: string;
  enabled: boolean;
}

// Mirrors backend AdminRegisterUserRequestDto (POST /api/admin/users/register).
export interface AdminRegisterUserRequestDto {
  username: string;
  password: string;
  role: string; // "USER" | "ADMIN"
}

// Mirrors backend AdminDisableUserRequestDto (POST /api/admin/users/disable).
export interface AdminDisableUserRequestDto {
  username: string;
}
