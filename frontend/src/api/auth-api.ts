import { apiClient } from "./config";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/auth";

export const authApi = {
  async login(payload: LoginRequest) {
    const response = await apiClient.post<AuthResponse>("/auth/login", payload);
    return response.data;
  },

  async register(payload: RegisterRequest) {
    const response = await apiClient.post<AuthResponse>("/auth/register", payload);
    return response.data;
  },

  async checkEmailExists(email: string): Promise<boolean> {
    const response = await apiClient.get<{ exists: boolean }>("/auth/check-email-exists", {
      params: { email },
    });
    return response.data.exists;
  },
};
