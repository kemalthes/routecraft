import { apiClient } from "./config";
import type { CurrentUserResponse } from "../types/user";

export const usersApi = {
  async getCurrentUser() {
    const response = await apiClient.get<CurrentUserResponse>("/users/me");
    return response.data;
  },
};
