import { apiClient } from "@/lib/api/axios-client";
import type { AssetInput, LiabilityInput, NetWorthHistory, NetWorthSummary, UserAsset, UserLiability } from "@/types/net-worth";

export const netWorthApi = {
  getSummary: () => apiClient.get<NetWorthSummary>("/api/net-worth/summary").then((res) => res.data),

  getHistory: () => apiClient.get<NetWorthHistory>("/api/net-worth/history").then((res) => res.data),

  listAssets: () => apiClient.get<UserAsset[]>("/api/net-worth/assets").then((res) => res.data),

  createAsset: (input: AssetInput) => apiClient.post<UserAsset>("/api/net-worth/assets", input).then((res) => res.data),

  updateAsset: (assetId: string, input: AssetInput) =>
    apiClient.put<UserAsset>(`/api/net-worth/assets/${assetId}`, input).then((res) => res.data),

  deleteAsset: (assetId: string) => apiClient.delete<void>(`/api/net-worth/assets/${assetId}`).then((res) => res.data),

  listLiabilities: () => apiClient.get<UserLiability[]>("/api/net-worth/liabilities").then((res) => res.data),

  createLiability: (input: LiabilityInput) =>
    apiClient.post<UserLiability>("/api/net-worth/liabilities", input).then((res) => res.data),

  updateLiability: (liabilityId: string, input: LiabilityInput) =>
    apiClient.put<UserLiability>(`/api/net-worth/liabilities/${liabilityId}`, input).then((res) => res.data),

  deleteLiability: (liabilityId: string) =>
    apiClient.delete<void>(`/api/net-worth/liabilities/${liabilityId}`).then((res) => res.data),
};
