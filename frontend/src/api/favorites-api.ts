import { apiClient } from "./config";
import type { PaginatedRoutesResponse } from "../types/routes";

export const favoritesApi = {
  async getFavorites(params?: { page?: number; limit?: number }) {
    const response = await apiClient.get<PaginatedRoutesResponse>("/favorites", {
      params: {
        page: params?.page ?? 1,
        limit: params?.limit ?? 12,
      },
    });
    return response.data;
  },

  async addFavorite(routeId: string) {
    await apiClient.post(`/favorites/${routeId}`);
  },

  async removeFavorite(routeId: string) {
    await apiClient.delete(`/favorites/${routeId}`);
  },
};
