import { apiClient } from "./config";
import type {
  DeleteRouteRequest,
  PaginatedRoutesResponse,
  UpdateRouteRequest,
} from "../types/routes";
import type { PaginatedUserResponse } from "../types/user";

export const adminApi = {
  async getUsers(params?: { page?: number; limit?: number }) {
    const response = await apiClient.get<PaginatedUserResponse>("/admin/users", {
      params: {
        page: params?.page ?? 1,
        limit: params?.limit ?? 10,
      },
    });
    return response.data;
  },

  async getRoutes(params?: {
    page?: number;
    limit?: number;
    status?: "DRAFT" | "PENDING" | "PUBLISHED" | string;
  }) {
    const response = await apiClient.get<PaginatedRoutesResponse>("/admin/routes", {
      params: {
        page: params?.page ?? 1,
        limit: params?.limit ?? 10,
        status: params?.status,
      },
    });
    return response.data;
  },

  async approveRoute(payload: UpdateRouteRequest) {
    const response = await apiClient.put<string>("/admin/routes", payload);
    return response.data;
  },

  async deleteRoute(payload: DeleteRouteRequest) {
    await apiClient.delete("/admin/routes", { data: payload });
  },
};
