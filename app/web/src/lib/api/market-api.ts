import { apiClient } from "@/lib/api/axios-client";
import type { MarketIndex, MarketStatus, StockQuote } from "@/types/stock";

export const marketApi = {
  getIndices: () => apiClient.get<MarketIndex[]>("/api/market/indices").then((res) => res.data),

  getGainers: () => apiClient.get<StockQuote[]>("/api/market/gainers").then((res) => res.data),

  getLosers: () => apiClient.get<StockQuote[]>("/api/market/losers").then((res) => res.data),

  getMostActive: () => apiClient.get<StockQuote[]>("/api/market/most-active").then((res) => res.data),

  getStatus: (exchange = "NSE") => apiClient.get<MarketStatus>("/api/market/status", { params: { exchange } }).then((res) => res.data),
};
