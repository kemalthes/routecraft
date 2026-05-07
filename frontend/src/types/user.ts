export interface CurrentUserResponse {
  id: string;
  username: string;
  email: string;
  role: "USER" | "ADMIN" | string;
  createdAt?: string;
}

export interface PaginatedUserResponse {
  items: CurrentUserResponse[];
  meta: {
    totalItems: number;
    totalPages: number;
    currentPage: number;
    itemsPerPage: number;
  };
}
