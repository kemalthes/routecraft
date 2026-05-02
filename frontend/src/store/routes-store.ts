import { create } from "zustand";
import { routesApi } from "../api/routes-api";
import { ApiClientError } from "../types/api";
import type {
  CreateRouteFormValues,
  PaginationMeta,
  RouteFull,
  RoutePreview,
} from "../types/routes";

const defaultPagination: PaginationMeta = {
  totalItems: 0,
  totalPages: 0,
  currentPage: 1,
  itemsPerPage: 12,
};

interface FetchRoutesParams {
  page?: number;
  limit?: number;
  search?: string;
}

interface RoutesState {
  routes: RoutePreview[];
  pagination: PaginationMeta;
  routesLoading: boolean;
  routeLoading: boolean;
  creating: boolean;
  selectedRouteId: string | null;
  selectedRoute: RouteFull | null;
  errorMessage: string | null;
  fetchRoutes: (params?: FetchRoutesParams) => Promise<void>;
  selectRoute: (routeId: string) => Promise<void>;
  clearSelection: () => void;
  createRoute: (form: CreateRouteFormValues, file?: File) => Promise<string>;
}

export const useRoutesStore = create<RoutesState>()((set) => ({
  routes: [],
  pagination: defaultPagination,
  routesLoading: false,
  routeLoading: false,
  creating: false,
  selectedRouteId: null,
  selectedRoute: null,
  errorMessage: null,

  fetchRoutes: async (params) => {
    set({ routesLoading: true, errorMessage: null });
    try {
      const response = await routesApi.getRoutes(params);
      set({
        routes: response.items ?? [],
        pagination: response.meta ?? defaultPagination,
      });
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to fetch routes";
      set({ errorMessage: message });
    } finally {
      set({ routesLoading: false });
    }
  },

  selectRoute: async (routeId) => {
    set({ selectedRouteId: routeId, routeLoading: true, errorMessage: null });
    try {
      const route = await routesApi.getRouteById(routeId);
      set({ selectedRoute: route });
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to fetch route";
      set({
        selectedRoute: null,
        errorMessage: message,
      });
    } finally {
      set({ routeLoading: false });
    }
  },

  clearSelection: () => {
    set({ selectedRouteId: null, selectedRoute: null });
  },

  createRoute: async (form, file) => {
    set({ creating: true, errorMessage: null });
    try {
      const routeId = await routesApi.createRouteWithImage({
        form,
        file,
      });
      return routeId;
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to create route";
      set({ errorMessage: message });
      throw error;
    } finally {
      set({ creating: false });
    }
  },
}));
