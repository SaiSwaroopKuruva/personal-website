"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { netWorthApi } from "@/lib/api/net-worth-api";
import type { AssetInput, LiabilityInput } from "@/types/net-worth";

export function useNetWorthSummary() {
  return useQuery({ queryKey: ["net-worth", "summary"], queryFn: netWorthApi.getSummary });
}

export function useNetWorthHistory() {
  return useQuery({ queryKey: ["net-worth", "history"], queryFn: netWorthApi.getHistory });
}

export function useNetWorthAssets() {
  return useQuery({ queryKey: ["net-worth", "assets"], queryFn: netWorthApi.listAssets });
}

export function useNetWorthLiabilities() {
  return useQuery({ queryKey: ["net-worth", "liabilities"], queryFn: netWorthApi.listLiabilities });
}

function invalidateNetWorth(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: ["net-worth"] });
}

export function useCreateAsset() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: AssetInput) => netWorthApi.createAsset(input),
    onSuccess: () => {
      invalidateNetWorth(queryClient);
      toast.success("Asset added");
    },
    onError: () => toast.error("Could not add this asset"),
  });
}

export function useUpdateAsset() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ assetId, input }: { assetId: string; input: AssetInput }) => netWorthApi.updateAsset(assetId, input),
    onSuccess: () => {
      invalidateNetWorth(queryClient);
      toast.success("Asset updated");
    },
    onError: () => toast.error("Could not update this asset"),
  });
}

export function useDeleteAsset() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (assetId: string) => netWorthApi.deleteAsset(assetId),
    onSuccess: () => {
      invalidateNetWorth(queryClient);
      toast.success("Asset removed");
    },
    onError: () => toast.error("Could not remove this asset"),
  });
}

export function useCreateLiability() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: LiabilityInput) => netWorthApi.createLiability(input),
    onSuccess: () => {
      invalidateNetWorth(queryClient);
      toast.success("Liability added");
    },
    onError: () => toast.error("Could not add this liability"),
  });
}

export function useUpdateLiability() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ liabilityId, input }: { liabilityId: string; input: LiabilityInput }) =>
      netWorthApi.updateLiability(liabilityId, input),
    onSuccess: () => {
      invalidateNetWorth(queryClient);
      toast.success("Liability updated");
    },
    onError: () => toast.error("Could not update this liability"),
  });
}

export function useDeleteLiability() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (liabilityId: string) => netWorthApi.deleteLiability(liabilityId),
    onSuccess: () => {
      invalidateNetWorth(queryClient);
      toast.success("Liability removed");
    },
    onError: () => toast.error("Could not remove this liability"),
  });
}
