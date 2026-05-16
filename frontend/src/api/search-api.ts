import { apiClient } from "./config";
import type { RoutePreview } from "../types/routes";

export const searchApi = {
  async searchRoutes(params: { q: string; limit?: number }) {
    const response = await apiClient.get<RoutePreview[]>("/search", {
      params: {
        q: params.q,
        limit: params.limit ?? 12,
      },
    });
    return response.data;
  },
};
