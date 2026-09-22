import { apiClient } from "@/lib/api/axios-client";
import type { CandleHistory, StockDetails, StockNewsItem, StockQuote } from "@/types/stock";

export const stockApi = {
  search: (query: string) => apiClient.get<StockDetails[]>("/api/stocks", { params: { query } }).then((res) => res.data),

  getDetails: (symbol: string) => apiClient.get<StockDetails>(`/api/stocks/${symbol}`).then((res) => res.data),

  getCurrentPrice: (symbol: string) => apiClient.get<StockQuote>(`/api/stocks/${symbol}/prices`).then((res) => res.data),

  getOhlc: (symbol: string, params: { from?: string; to?: string; interval?: string } = {}) =>
    apiClient.get<CandleHistory>(`/api/stocks/${symbol}/ohlc`, { params }).then((res) => res.data),

  getNews: (symbol: string) => apiClient.get<StockNewsItem[]>(`/api/stocks/${symbol}/news`).then((res) => res.data),
};
