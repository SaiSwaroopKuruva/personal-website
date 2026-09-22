"use client";

import { useQuery } from "@tanstack/react-query";
import { providerStatusApi } from "@/lib/api/provider-status-api";

export function useProviderStatus() {
  return useQuery({
    queryKey: ["data-providers", "status"],
    queryFn: providerStatusApi.getStatus,
    staleTime: 60_000,
  });
}
