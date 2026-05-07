import { create } from "zustand";
import { favoritesApi } from "../api/favorites-api";
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
  favorites: RoutePreview[];
  myRoutes: RoutePreview[];
  pagination: PaginationMeta;
  favoritesPagination: PaginationMeta;
  myRoutesPagination: PaginationMeta;
  reviews: ReviewResponse[];
  reviewsPagination: PaginationMeta;
  routesLoading: boolean;
  routeLoading: boolean;
  reviewsLoading: boolean;
  creating: boolean;
  creatingReview: boolean;
  deletingReviewId: string | null;
  togglingFavoriteId: string | null;
  selectedRouteId: string | null;
  selectedRoute: RouteFull | null;
  errorMessage: string | null;
  fetchRoutes: (params?: FetchRoutesParams) => Promise<void>;
  fetchFavorites: (params?: { page?: number; limit?: number }) => Promise<void>;
  fetchMyRoutes: (params?: { page?: number; limit?: number }) => Promise<void>;
  selectRoute: (routeId: string) => Promise<void>;
  fetchReviews: (routeId: string, params?: { page?: number; limit?: number }) => Promise<void>;
  createReview: (routeId: string, payload: ReviewRequest) => Promise<ReviewResponse>;
  deleteReview: (routeId: string, reviewId: string) => Promise<void>;
  toggleFavorite: (routeId: string, nextLiked: boolean) => Promise<void>;
  updateRoute: (payload: { uuid: string; version?: number; title: string; description?: string }) => Promise<void>;
  deleteRoute: (routeId: string, version?: number) => Promise<void>;
  clearSelection: () => void;
  createRoute: (form: CreateRouteFormValues, file?: File) => Promise<string>;
}

export const useRoutesStore = create<RoutesState>()((set, get) => ({
  routes: [],
  favorites: [],
  myRoutes: [],
  pagination: defaultPagination,
  favoritesPagination: defaultPagination,
  myRoutesPagination: defaultPagination,
  reviews: [],
  reviewsPagination: defaultPagination,
  routesLoading: false,
  routeLoading: false,
  reviewsLoading: false,
  creating: false,
  creatingReview: false,
  deletingReviewId: null,
  togglingFavoriteId: null,
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

  fetchFavorites: async (params) => {
    set({ routesLoading: true, errorMessage: null });
    try {
      const response = await favoritesApi.getFavorites(params);
      set({
        favorites: (response.items ?? []).map((route) => ({ ...route, is_liked: true })),
        favoritesPagination: response.meta ?? defaultPagination,
      });
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to fetch favorites";
      set({ errorMessage: message });
    } finally {
      set({ routesLoading: false });
    }
  },

  fetchMyRoutes: async (params) => {
    set({ routesLoading: true, errorMessage: null });
    try {
      const response = await routesApi.getMyRoutes(params);
      set({
        myRoutes: response.items ?? [],
        myRoutesPagination: response.meta ?? defaultPagination,
      });
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to fetch your routes";
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

  toggleFavorite: async (routeId, nextLiked) => {
    const previousRoutes = get().routes;
    const previousFavorites = get().favorites;
    const previousSelectedRoute = get().selectedRoute;
    const applyLiked = (route: RoutePreview) =>
      route.id === routeId ? { ...route, is_liked: nextLiked } : route;

    set({
      togglingFavoriteId: routeId,
      routes: previousRoutes.map(applyLiked),
      favorites: nextLiked
        ? previousFavorites
        : previousFavorites.filter((route) => route.id !== routeId),
      selectedRoute:
        previousSelectedRoute?.id === routeId
          ? { ...previousSelectedRoute, is_liked: nextLiked }
          : previousSelectedRoute,
      errorMessage: null,
    });

    try {
      if (nextLiked) {
        await favoritesApi.addFavorite(routeId);
      } else {
        await favoritesApi.removeFavorite(routeId);
      }
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to update favorites";
      set({
        routes: previousRoutes,
        favorites: previousFavorites,
        selectedRoute: previousSelectedRoute,
        errorMessage: message,
      });
      throw error;
    } finally {
      set({ togglingFavoriteId: null });
    }
  },

  updateRoute: async (payload) => {
    set({ errorMessage: null });
    try {
      await routesApi.updateRoute(payload);
      const page = get().myRoutesPagination.currentPage || 1;
      const limit = get().myRoutesPagination.itemsPerPage || 10;
      await get().fetchMyRoutes({ page, limit });
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to update route";
      set({ errorMessage: message });
      throw error;
    }
  },

  deleteRoute: async (routeId, version) => {
    set({ errorMessage: null });
    try {
      await routesApi.deleteRoute(routeId, version);
      const page = get().myRoutesPagination.currentPage || 1;
      const limit = get().myRoutesPagination.itemsPerPage || 10;
      await get().fetchMyRoutes({ page, limit });
    } catch (error) {
      const message =
        error instanceof ApiClientError ? error.message : "Failed to delete route";
      set({ errorMessage: message });
      throw error;
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
