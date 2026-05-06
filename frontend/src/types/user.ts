export interface CurrentUserResponse {
  id: string;
  username: string;
  email: string;
  role: "USER" | "ADMIN" | string;
  createdAt?: string;
}
