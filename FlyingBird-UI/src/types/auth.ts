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
