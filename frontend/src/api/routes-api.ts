import { apiClient, uploadClient } from "./config";
import type {
  CreateRouteFormValues,
  CreateRouteRequest,
  CreateRouteResponse,
  PaginatedReviewsResponse,
  PaginatedRoutesResponse,
  ReviewRequest,
  ReviewResponse,
  RouteFull,
  RouteLocationDto,
  UpdateRouteRequest,
} from "../types/routes";

const mapFormLocationsToDto = (
  points: CreateRouteFormValues["locations"],
): RouteLocationDto[] =>
  points.map((point, index) => ({
    orderIndex: index,
    latitude: point.lat,
    longitude: point.lng,
  }));

export const routesApi = {
  async getRoutes(params?: { page?: number; limit?: number; search?: string }) {
    const response = await apiClient.get<PaginatedRoutesResponse>("/routes", {
      params: {
        page: params?.page ?? 1,
        limit: params?.limit ?? 12,
        search: params?.search || undefined,
      },
    });
    return response.data;
  },

  async getRouteById(routeId: string) {
    const response = await apiClient.get<RouteFull>(`/routes/${routeId}`);
    return response.data;
  },

  async getMyRoutes(params?: { page?: number; limit?: number }) {
    const response = await apiClient.get<PaginatedRoutesResponse>("/routes/my", {
      params: {
        page: params?.page ?? 1,
        limit: params?.limit ?? 10,
      },
    });
    return response.data;
  },

  async getMyRouteById(routeId: string) {
    const response = await apiClient.get<RouteFull>(`/routes/my/${routeId}`);
    return response.data;
  },

  async getReviewsByRoute(routeId: string, params?: { page?: number; limit?: number }) {
    const response = await apiClient.get<PaginatedReviewsResponse>(`/routes/${routeId}/reviews`, {
      params: {
        page: params?.page ?? 1,
        limit: params?.limit ?? 10,
      },
    });
    return response.data;
  },

  async createReview(routeId: string, payload: ReviewRequest) {
    const response = await apiClient.post<ReviewResponse>(`/routes/${routeId}/reviews`, payload);
    return response.data;
  },

  async deleteReview(routeId: string, reviewId: string) {
    await apiClient.delete(`/routes/${routeId}/reviews/${reviewId}`);
  },

  async updateRoute(payload: UpdateRouteRequest) {
    const response = await apiClient.put<string>("/routes", payload);
    return response.data;
  },

  async deleteRoute(routeId: string, version?: number) {
    await apiClient.delete(`/routes/${routeId}`, {
      params: version === undefined ? undefined : { version },
    });
  },

  async createRouteWithImage(data: { form: CreateRouteFormValues; file?: File }) {
    const payload: CreateRouteRequest = {
      title: data.form.title.trim(),
      description: data.form.description.trim() || undefined,
      imageUrl: data.file?.name,
      locations: mapFormLocationsToDto(data.form.locations),
    };

    const createResponse = await apiClient.post<CreateRouteResponse>("/routes", payload);
    const { uuid, imageUrl } = createResponse.data;

    if (data.file) {
      await uploadClient.put(imageUrl, data.file, {
        headers: {
          "Content-Type": data.file.type || "application/octet-stream",
        },
      });
    }

    return uuid;
  },
};
