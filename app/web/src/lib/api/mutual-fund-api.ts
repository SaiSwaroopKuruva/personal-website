import { apiClient } from "@/lib/api/axios-client";
import type { PageResponse } from "@/types/security";
import type {
  ComparisonResult,
  FavoriteFund,
  FavoriteStatus,
  FilterMetadata,
  FundHolding,
  FundManager,
  MutualFundDetails,
  MutualFundSearchParams,
  MutualFundSummary,
  NavHistory,
  ReturnsResponse,
} from "@/types/mutual-fund";

export const mutualFundApi = {
  search: (params: MutualFundSearchParams) =>
    apiClient
      .get<PageResponse<MutualFundSummary>>("/api/mutual-funds", { params })
      .then((res) => res.data),

  getFilters: () => apiClient.get<FilterMetadata>("/api/mutual-funds/filters").then((res) => res.data),

  getDetails: (schemeCode: string) =>
    apiClient.get<MutualFundDetails>(`/api/mutual-funds/${schemeCode}`).then((res) => res.data),

  getNavHistory: (schemeCode: string, params: { from?: string; to?: string; interval?: string } = {}) =>
    apiClient
      .get<NavHistory>(`/api/mutual-funds/${schemeCode}/nav-history`, { params })
      .then((res) => res.data),

  getReturns: (schemeCode: string) =>
    apiClient.get<ReturnsResponse>(`/api/mutual-funds/${schemeCode}/returns`).then((res) => res.data),

  getHoldings: (schemeCode: string, page = 0, size = 20) =>
    apiClient
      .get<PageResponse<FundHolding>>(`/api/mutual-funds/${schemeCode}/holdings`, { params: { page, size } })
      .then((res) => res.data),

  getManagers: (schemeCode: string) =>
    apiClient.get<FundManager[]>(`/api/mutual-funds/${schemeCode}/managers`).then((res) => res.data),

  compare: (schemeCodes: string[]) =>
    apiClient
      .get<ComparisonResult>("/api/mutual-funds/compare", { params: { schemes: schemeCodes.join(",") } })
      .then((res) => res.data),

  getFavorites: (page = 0, size = 20) =>
    apiClient
      .get<PageResponse<FavoriteFund>>("/api/mutual-funds/favorites", { params: { page, size } })
      .then((res) => res.data),

  addFavorite: (schemeCode: string) =>
    apiClient.post<FavoriteStatus>(`/api/mutual-funds/${schemeCode}/favorite`).then((res) => res.data),

  removeFavorite: (schemeCode: string) =>
    apiClient.delete<FavoriteStatus>(`/api/mutual-funds/${schemeCode}/favorite`).then((res) => res.data),
};
