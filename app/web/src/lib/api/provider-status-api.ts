import { apiClient } from "@/lib/api/axios-client";
import type { ProviderStatusResponse } from "@/types/provider-status";

export const providerStatusApi = {
  getStatus: () => apiClient.get<ProviderStatusResponse>("/api/data-providers/status").then((res) => res.data),
};
