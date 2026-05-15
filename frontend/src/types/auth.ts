export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  code: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  role: "ROLE_USER" | "ROLE_ADMIN";
}

export interface AuthCodeRequest {
  email: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
}
