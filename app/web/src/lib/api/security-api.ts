import { apiClient } from "@/lib/api/axios-client";
import type { DeviceResponse, LoginHistoryEntry, PageResponse } from "@/types/security";

export const securityApi = {
  getDevices: () => apiClient.get<DeviceResponse[]>("/api/security/devices").then((res) => res.data),

  getLoginHistory: (page = 0, size = 20) =>
    apiClient
      .get<PageResponse<LoginHistoryEntry>>("/api/security/login-history", { params: { page, size } })
      .then((res) => res.data),

  logoutAllDevices: () => apiClient.post<void>("/api/security/logout-all").then((res) => res.data),

  revokeDevice: (id: string) => apiClient.delete<void>(`/api/security/device/${id}`).then((res) => res.data),
};

export const passwordApi = {
  forgotPassword: (email: string) => apiClient.post<void>("/api/password/forgot", { email }).then((res) => res.data),

  resetPassword: (token: string, newPassword: string) =>
    apiClient.post<void>("/api/password/reset", { token, newPassword }).then((res) => res.data),

  changePassword: (currentPassword: string, newPassword: string) =>
    apiClient.post<void>("/api/password/change", { currentPassword, newPassword }).then((res) => res.data),
};

export const emailVerificationApi = {
  sendVerification: () => apiClient.post<void>("/api/email/send-verification").then((res) => res.data),

  verify: (token: string) => apiClient.post<void>("/api/email/verify", { token }).then((res) => res.data),

  resend: () => apiClient.post<void>("/api/email/resend").then((res) => res.data),
};
