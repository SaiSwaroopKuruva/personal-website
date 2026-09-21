"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { mutualFundApi } from "@/lib/api/mutual-fund-api";
import { useAuthStore } from "@/store/auth-store";
import type { MutualFundSearchParams } from "@/types/mutual-fund";

export function useMutualFundSearch(params: MutualFundSearchParams) {
  return useQuery({
    queryKey: ["mutual-funds", "search", params],
    queryFn: () => mutualFundApi.search(params),
    placeholderData: (previousData) => previousData,
  });
}

export function useMutualFundFilters() {
  return useQuery({
    queryKey: ["mutual-funds", "filters"],
    queryFn: mutualFundApi.getFilters,
    staleTime: 60 * 60 * 1000,
  });
}

export function useMutualFundDetails(schemeCode: string) {
  return useQuery({
    queryKey: ["mutual-funds", "details", schemeCode],
    queryFn: () => mutualFundApi.getDetails(schemeCode),
    enabled: !!schemeCode,
  });
}

export function useMutualFundNavHistory(schemeCode: string, range: string) {
  return useQuery({
    queryKey: ["mutual-funds", "nav-history", schemeCode, range],
    queryFn: () => mutualFundApi.getNavHistory(schemeCode, rangeToParams(range)),
    enabled: !!schemeCode,
  });
}

export function useMutualFundReturns(schemeCode: string) {
  return useQuery({
    queryKey: ["mutual-funds", "returns", schemeCode],
    queryFn: () => mutualFundApi.getReturns(schemeCode),
    enabled: !!schemeCode,
  });
}

export function useMutualFundHoldings(schemeCode: string, page = 0, size = 20) {
  return useQuery({
    queryKey: ["mutual-funds", "holdings", schemeCode, page, size],
    queryFn: () => mutualFundApi.getHoldings(schemeCode, page, size),
    enabled: !!schemeCode,
  });
}

export function useMutualFundManagers(schemeCode: string) {
  return useQuery({
    queryKey: ["mutual-funds", "managers", schemeCode],
    queryFn: () => mutualFundApi.getManagers(schemeCode),
    enabled: !!schemeCode,
  });
}

export function useMutualFundComparison(schemeCodes: string[]) {
  return useQuery({
    queryKey: ["mutual-funds", "compare", schemeCodes],
    queryFn: () => mutualFundApi.compare(schemeCodes),
    enabled: schemeCodes.length >= 2,
  });
}

export function useFavoriteMutualFunds(page = 0, size = 20) {
  const accessToken = useAuthStore((state) => state.accessToken);
  return useQuery({
    queryKey: ["mutual-funds", "favorites", page, size],
    queryFn: () => mutualFundApi.getFavorites(page, size),
    enabled: !!accessToken,
  });
}

export function useToggleFavorite() {
  const queryClient = useQueryClient();

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["mutual-funds"] });
  };

  const add = useMutation({
    mutationFn: (schemeCode: string) => mutualFundApi.addFavorite(schemeCode),
    onSuccess: () => {
      invalidate();
      toast.success("Added to favorites");
    },
    onError: () => toast.error("Could not add this fund to favorites"),
  });

  const remove = useMutation({
    mutationFn: (schemeCode: string) => mutualFundApi.removeFavorite(schemeCode),
    onSuccess: () => {
      invalidate();
      toast.success("Removed from favorites");
    },
    onError: () => toast.error("Could not remove this fund from favorites"),
  });

  return { add, remove };
}

function rangeToParams(range: string): { from?: string; interval?: string } {
  const today = new Date();
  const from = new Date(today);
  let interval = "DAILY";

  switch (range) {
    case "1M":
      from.setMonth(from.getMonth() - 1);
      break;
    case "3M":
      from.setMonth(from.getMonth() - 3);
      break;
    case "6M":
      from.setMonth(from.getMonth() - 6);
      interval = "WEEKLY";
      break;
    case "1Y":
      from.setFullYear(from.getFullYear() - 1);
      interval = "WEEKLY";
      break;
    case "3Y":
      from.setFullYear(from.getFullYear() - 3);
      interval = "MONTHLY";
      break;
    case "5Y":
      from.setFullYear(from.getFullYear() - 5);
      interval = "MONTHLY";
      break;
    case "MAX":
      from.setFullYear(from.getFullYear() - 20);
      interval = "MONTHLY";
      break;
    default:
      from.setFullYear(from.getFullYear() - 1);
      interval = "WEEKLY";
  }

  return { from: from.toISOString().slice(0, 10), interval };
}
