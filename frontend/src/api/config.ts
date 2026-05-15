import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";
import {
  ApiClientError,
  isErrorResponse,
  isValidationErrorResponse,
  parseFieldValidationErrors,
} from "../types/api";
import type { AuthResponse } from "../types/auth";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

export const uploadClient = axios.create({
  timeout: 30000,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshPromise: Promise<AuthResponse> | null = null;

const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    throw new Error("Refresh token отсутствует");
  }
  if (!refreshPromise) {
    refreshPromise = axios
      .post<AuthResponse>(`${API_BASE_URL}/auth/refresh`, { refreshToken })
      .then((response) => response.data)
      .finally(() => {
        refreshPromise = null;
      });
  }
  const result = await refreshPromise;
  localStorage.setItem("accessToken", result.accessToken);
  localStorage.setItem("refreshToken", result.refreshToken);
  localStorage.setItem("role", result.role);
  return result.accessToken;
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    if (!axios.isAxiosError(error) || error.response?.status !== 401 || !error.config) {
      return Promise.reject(error);
    }
    const config = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const url = config.url ?? "";
    const canRefresh = !config._retry && !url.includes("/auth/login") && !url.includes("/auth/register")
      && !url.includes("/auth/refresh") && !url.includes("/auth/logout");
    if (!canRefresh) {
      return Promise.reject(error);
    }
    try {
      config._retry = true;
      const accessToken = await refreshAccessToken();
      config.headers.Authorization = `Bearer ${accessToken}`;
      return apiClient(config);
    } catch (refreshError) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("role");
      return Promise.reject(refreshError);
    }
  },
);

export const toApiClientError = (error: unknown): ApiClientError => {
  if (!axios.isAxiosError(error)) {
    return new ApiClientError({
      message: "Неожиданная ошибка клиента",
    });
  }

  const axiosError = error as AxiosError;
  const status = axiosError.response?.status;
  const payload = axiosError.response?.data;

  if (isValidationErrorResponse(payload)) {
    return new ApiClientError({
      message: payload.message,
      status,
      error: payload.error,
      path: payload.path,
      validationErrors: parseFieldValidationErrors(payload.validationErrors),
    });
  }

  if (isErrorResponse(payload)) {
    return new ApiClientError({
      message: payload.message,
      status,
      error: payload.error,
      path: payload.path,
    });
  }

  return new ApiClientError({
    message: axiosError.message || "Запрос не выполнен",
    status,
  });
};

apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => Promise.reject(toApiClientError(error)),
);

uploadClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => Promise.reject(toApiClientError(error)),
);
