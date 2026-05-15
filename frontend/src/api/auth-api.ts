import { apiClient } from "./config";
import type {
  AuthResponse,
  ChangePasswordRequest,
  LoginRequest,
  RegisterRequest,
  ResetPasswordRequest,
} from "../types/auth";

export const authApi = {
  async sendRegistrationCode(email: string) {
    await apiClient.post<void>("/auth/registration-code", { email });
  },

  async login(payload: LoginRequest) {
    const response = await apiClient.post<AuthResponse>("/auth/login", payload);
    return response.data;
  },

  async register(payload: RegisterRequest) {
    const response = await apiClient.post<AuthResponse>("/auth/register", payload);
    return response.data;
  },

  async refresh(refreshToken: string) {
    const response = await apiClient.post<AuthResponse>("/auth/refresh", { refreshToken });
    return response.data;
  },

  async logout() {
    await apiClient.post<void>("/auth/logout");
  },

  async changePassword(payload: ChangePasswordRequest) {
    await apiClient.post<void>("/auth/change-password", payload);
  },

  async sendPasswordResetCode(email: string) {
    await apiClient.post<void>("/auth/password-reset-code", { email });
  },

  async resetPassword(payload: ResetPasswordRequest) {
    await apiClient.post<void>("/auth/reset-password", payload);
  },

  async checkEmailExists(email: string): Promise<boolean> {
    const response = await apiClient.get<{ exists: boolean }>("/auth/check-email-exists", {
      params: { email },
    });
    return response.data.exists;
  },
};
