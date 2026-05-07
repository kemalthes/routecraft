import axios, { type AxiosError } from "axios";
import {
  ApiClientError,
  isErrorResponse,
  isValidationErrorResponse,
  parseFieldValidationErrors,
} from "../types/api";

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

export const toApiClientError = (error: unknown): ApiClientError => {
  if (!axios.isAxiosError(error)) {
    return new ApiClientError({
      message: "Unexpected client error",
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
    message: axiosError.message || "Request failed",
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
