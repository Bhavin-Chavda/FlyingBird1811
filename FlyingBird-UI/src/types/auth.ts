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

export interface User {
  username: string;
  role: string;
}
