import { apiClient } from "@/lib/api/axios-client";
import type { AuthResponse, LoginPayload, RegisterPayload, User } from "@/types/auth";

export const authApi = {
  register: (payload: RegisterPayload) =>
    apiClient.post<AuthResponse>("/api/auth/register", payload).then((res) => res.data),

  login: (payload: LoginPayload) =>
    apiClient.post<AuthResponse>("/api/auth/login", payload).then((res) => res.data),

  logout: (refreshToken: string) =>
    apiClient.post<void>("/api/auth/logout", { refreshToken }).then((res) => res.data),

  me: () => apiClient.get<User>("/api/auth/me").then((res) => res.data),
};
