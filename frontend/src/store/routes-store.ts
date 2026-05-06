import { create } from "zustand";
import { routesApi } from "../api/routes-api";
import { ApiClientError } from "../types/api";
import type {
  CreateRouteFormValues,
  PaginationMeta,
  ReviewRequest,
  ReviewResponse,
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
  reviews: ReviewResponse[];
  reviewsPagination: PaginationMeta;
  routesLoading: boolean;
  routeLoading: boolean;
  reviewsLoading: boolean;
  creating: boolean;
  creatingReview: boolean;
  deletingReviewId: string | null;
  selectedRouteId: string | null;
  selectedRoute: RouteFull | null;
  errorMessage: string | null;
  fetchRoutes: (params?: FetchRoutesParams) => Promise<void>;
  selectRoute: (routeId: string) => Promise<void>;
  fetchReviews: (routeId: string, params?: { page?: number; limit?: number }) => Promise<void>;
  createReview: (routeId: string, payload: ReviewRequest) => Promise<ReviewResponse>;
  deleteReview: (routeId: string, reviewId: string) => Promise<void>;
  clearSelection: () => void;
  createRoute: (form: CreateRouteFormValues, file?: File) => Promise<string>;
}

export const useRoutesStore = create<RoutesState>()((set, get) => ({
  routes: [],
  pagination: defaultPagination,
  reviews: [],
  reviewsPagination: defaultPagination,
  routesLoading: false,
  routeLoading: false,
  reviewsLoading: false,
  creating: false,
  creatingReview: false,
  deletingReviewId: null,
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

  fetchReviews: async (routeId, params) => {
    set({ reviewsLoading: true, errorMessage: null });
    try {
      const response = await routesApi.getReviewsByRoute(routeId, params);
      set({
        reviews: response.items ?? [],
        reviewsPagination: response.meta ?? defaultPagination,
      });
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to fetch reviews";
      set({ errorMessage: message });
    } finally {
      set({ reviewsLoading: false });
    }
  },

  createReview: async (routeId, payload) => {
    set({ creatingReview: true, errorMessage: null });
    try {
      const createdReview = await routesApi.createReview(routeId, payload);
      const itemsPerPage = get().reviewsPagination.itemsPerPage || 10;
      await get().fetchReviews(routeId, { page: 1, limit: itemsPerPage });
      return createdReview;
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to create review";
      set({ errorMessage: message });
      throw error;
    } finally {
      set({ creatingReview: false });
    }
  },

  deleteReview: async (routeId, reviewId) => {
    set({ deletingReviewId: reviewId, errorMessage: null });
    try {
      await routesApi.deleteReview(routeId, reviewId);
      const currentPage = get().reviewsPagination.currentPage || 1;
      const itemsPerPage = get().reviewsPagination.itemsPerPage || 10;
      await get().fetchReviews(routeId, { page: currentPage, limit: itemsPerPage });
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to delete review";
      set({ errorMessage: message });
      throw error;
    } finally {
      set({ deletingReviewId: null });
    }
  },

  clearSelection: () => {
    set({
      selectedRouteId: null,
      selectedRoute: null,
      reviews: [],
      reviewsPagination: defaultPagination,
      deletingReviewId: null,
    });
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
