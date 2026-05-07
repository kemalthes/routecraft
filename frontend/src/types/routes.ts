export interface RouteLocationFormPoint {
  orderIndex: number;
  lat: number;
  lng: number;
}

export interface RouteLocationDto {
  orderIndex: number;
  latitude: number;
  longitude: number;
}

export interface RoutePreview {
  id: string;
  version?: number;
  title: string;
  description?: string;
  imageUrl: string;
  distance: number;
  durationMinutes: number;
  authorName: string;
  status?: "DRAFT" | "PENDING" | "PUBLISHED" | string;
  is_liked?: boolean;
}

export interface ReviewResponse {
  id: string;
  authorName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface ReviewRequest {
  rating: number;
  comment: string;
}

export interface RouteFull {
  id: string;
  version?: number;
  title: string;
  description: string;
  imageUrl: string;
  distance: number;
  durationMinutes: number;
  authorName: string;
  status?: "DRAFT" | "PENDING" | "PUBLISHED" | string;
  locations: RouteLocationDto[];
  reviews?: ReviewResponse[];
  geometry?: string;
  is_liked?: boolean;
}

export interface PaginationMeta {
  totalItems: number;
  totalPages: number;
  currentPage: number;
  itemsPerPage: number;
}

export interface PaginatedRoutesResponse {
  items: RoutePreview[];
  meta: PaginationMeta;
}

export interface PaginatedReviewsResponse {
  items: ReviewResponse[];
  meta: PaginationMeta;
}

export interface CreateRouteRequest {
  title: string;
  description?: string;
  locations: RouteLocationDto[];
}

export interface UpdateRouteRequest {
  uuid: string;
  version?: number;
  title: string;
  description?: string;
}

export interface DeleteRouteRequest {
  uuid: string;
  version?: number;
}

export interface CreateRouteResponse {
  uuid: string;
  imageUrl: string;
}

export interface CreateRouteFormValues {
  title: string;
  description: string;
  locations: RouteLocationFormPoint[];
}
