import { apiClient, uploadClient } from "./config";
import type {
  CreateRouteFormValues,
  CreateRouteRequest,
  CreateRouteResponse,
  PaginatedRoutesResponse,
  RouteFull,
  RouteLocationDto,
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

  async createRouteWithImage(data: { form: CreateRouteFormValues; file?: File }) {
    const payload: CreateRouteRequest = {
      title: data.form.title.trim(),
      description: data.form.description.trim() || undefined,
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
