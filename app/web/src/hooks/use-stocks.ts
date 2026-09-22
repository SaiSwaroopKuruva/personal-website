"use client";

import { useQuery } from "@tanstack/react-query";
import { stockApi } from "@/lib/api/stock-api";

export function useStockSearch(query: string) {
  return useQuery({
    queryKey: ["stocks", "search", query],
    queryFn: () => stockApi.search(query),
    enabled: query.trim().length > 0,
  });
}

export function useStockDetails(symbol: string) {
  return useQuery({
    queryKey: ["stocks", "details", symbol],
    queryFn: () => stockApi.getDetails(symbol),
    enabled: !!symbol,
  });
}

/** Short refetch interval while the tab is active - never presents a value older than this without refreshing. */
export function useStockPrice(symbol: string) {
  return useQuery({
    queryKey: ["stocks", "price", symbol],
    queryFn: () => stockApi.getCurrentPrice(symbol),
    enabled: !!symbol,
    refetchInterval: 30_000,
  });
}

export function useStockOhlc(symbol: string, interval: "EOD" | "INTRADAY" = "EOD") {
  return useQuery({
    queryKey: ["stocks", "ohlc", symbol, interval],
    queryFn: () => stockApi.getOhlc(symbol, { interval }),
    enabled: !!symbol,
  });
}

export function useStockNews(symbol: string) {
  return useQuery({
    queryKey: ["stocks", "news", symbol],
    queryFn: () => stockApi.getNews(symbol),
    enabled: !!symbol,
  });
}
