"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { stockApi } from "@/lib/api/stock-api";
import { useAuthStore } from "@/store/auth-store";

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

export function useStockOhlc(symbol: string, params: { from?: string; to?: string; interval?: string } = { interval: "EOD" }) {
  return useQuery({
    queryKey: ["stocks", "ohlc", symbol, params],
    queryFn: () => stockApi.getOhlc(symbol, params),
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

export function useStockWatchlist() {
  const accessToken = useAuthStore((state) => state.accessToken);
  return useQuery({
    queryKey: ["stocks", "watchlist"],
    queryFn: () => stockApi.getWatchlist(),
    enabled: !!accessToken,
  });
}

export function useToggleWatchlist() {
  const queryClient = useQueryClient();

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["stocks", "watchlist"] });
  };

  const add = useMutation({
    mutationFn: (symbol: string) => stockApi.addToWatchlist(symbol),
    onSuccess: () => {
      invalidate();
      toast.success("Added to watchlist");
    },
    onError: () => toast.error("Could not add this stock to your watchlist"),
  });

  const remove = useMutation({
    mutationFn: (symbol: string) => stockApi.removeFromWatchlist(symbol),
    onSuccess: () => {
      invalidate();
      toast.success("Removed from watchlist");
    },
    onError: () => toast.error("Could not remove this stock from your watchlist"),
  });

  return { add, remove };
}

export function useStockComparison(symbols: string[]) {
  return useQuery({
    queryKey: ["stocks", "compare", symbols],
    queryFn: () => stockApi.compare(symbols),
    enabled: symbols.length >= 2,
  });
}
