"use client";

import { useQuery } from "@tanstack/react-query";
import { marketApi } from "@/lib/api/market-api";

export function useMarketIndices() {
  return useQuery({ queryKey: ["market", "indices"], queryFn: marketApi.getIndices, refetchInterval: 60_000 });
}

export function useMarketGainers() {
  return useQuery({ queryKey: ["market", "gainers"], queryFn: marketApi.getGainers, refetchInterval: 60_000 });
}

export function useMarketLosers() {
  return useQuery({ queryKey: ["market", "losers"], queryFn: marketApi.getLosers, refetchInterval: 60_000 });
}

export function useMarketMostActive() {
  return useQuery({ queryKey: ["market", "most-active"], queryFn: marketApi.getMostActive, refetchInterval: 60_000 });
}

export function useMarketStatus(exchange = "NSE") {
  return useQuery({ queryKey: ["market", "status", exchange], queryFn: () => marketApi.getStatus(exchange), refetchInterval: 60_000 });
}
