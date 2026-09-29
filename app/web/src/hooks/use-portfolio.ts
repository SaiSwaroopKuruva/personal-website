"use client";

import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { portfolioApi } from "@/lib/api/portfolio-api";
import { usePortfolioStore } from "@/store/portfolio-store";
import type { PortfolioInput, TransactionFilters, TransactionInput } from "@/types/portfolio";

export function usePortfolios() {
  return useQuery({ queryKey: ["portfolios"], queryFn: portfolioApi.list });
}

/** Auto-selects the user's default (or first) portfolio once portfolios load, if none is selected yet. */
export function useSelectedPortfolio() {
  const portfoliosQuery = usePortfolios();
  const selectedPortfolioId = usePortfolioStore((state) => state.selectedPortfolioId);
  const setSelectedPortfolioId = usePortfolioStore((state) => state.setSelectedPortfolioId);

  useEffect(() => {
    if (!portfoliosQuery.data || portfoliosQuery.data.length === 0) return;
    const stillExists = portfoliosQuery.data.some((p) => p.id === selectedPortfolioId);
    if (!selectedPortfolioId || !stillExists) {
      const defaultPortfolio = portfoliosQuery.data.find((p) => p.isDefault) ?? portfoliosQuery.data[0];
      setSelectedPortfolioId(defaultPortfolio.id);
    }
  }, [portfoliosQuery.data, selectedPortfolioId, setSelectedPortfolioId]);

  return {
    portfolios: portfoliosQuery.data ?? [],
    selectedPortfolioId,
    setSelectedPortfolioId,
    isLoading: portfoliosQuery.isLoading,
    isError: portfoliosQuery.isError,
  };
}

export function useCreatePortfolio() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: PortfolioInput) => portfolioApi.create(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["portfolios"] });
      toast.success("Portfolio created");
    },
    onError: () => toast.error("Could not create the portfolio"),
  });
}

export function useUpdatePortfolio(portfolioId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: PortfolioInput) => portfolioApi.update(portfolioId, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["portfolios"] });
      toast.success("Portfolio updated");
    },
    onError: () => toast.error("Could not update the portfolio"),
  });
}

export function useDeletePortfolio() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (portfolioId: string) => portfolioApi.remove(portfolioId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["portfolios"] });
      toast.success("Portfolio archived");
    },
    onError: () => toast.error("Could not archive the portfolio"),
  });
}

export function usePortfolioSummary(portfolioId: string | null) {
  return useQuery({
    queryKey: ["portfolios", "summary", portfolioId],
    queryFn: () => portfolioApi.getSummary(portfolioId as string),
    enabled: !!portfolioId,
  });
}

export function usePortfolioHoldings(portfolioId: string | null) {
  return useQuery({
    queryKey: ["portfolios", "holdings", portfolioId],
    queryFn: () => portfolioApi.getHoldings(portfolioId as string),
    enabled: !!portfolioId,
  });
}

export function usePortfolioAllocation(portfolioId: string | null) {
  return useQuery({
    queryKey: ["portfolios", "allocation", portfolioId],
    queryFn: () => portfolioApi.getAllocation(portfolioId as string),
    enabled: !!portfolioId,
  });
}

export function usePortfolioPerformance(portfolioId: string | null, range: string) {
  return useQuery({
    queryKey: ["portfolios", "performance", portfolioId, range],
    queryFn: () => portfolioApi.getPerformance(portfolioId as string, range),
    enabled: !!portfolioId,
  });
}

export function usePortfolioXirr(portfolioId: string | null) {
  return useQuery({
    queryKey: ["portfolios", "xirr", portfolioId],
    queryFn: () => portfolioApi.getXirr(portfolioId as string),
    enabled: !!portfolioId,
  });
}

export function usePortfolioTransactions(portfolioId: string | null, filters: TransactionFilters) {
  return useQuery({
    queryKey: ["portfolios", "transactions", portfolioId, filters],
    queryFn: () => portfolioApi.listTransactions(portfolioId as string, filters),
    enabled: !!portfolioId,
    placeholderData: (previousData) => previousData,
  });
}

/** Invalidates every derived view after a transaction mutation (Part 7/22) - holdings, gains, snapshots and net worth all depend on the ledger. */
function invalidatePortfolioDerivedData(queryClient: ReturnType<typeof useQueryClient>, portfolioId: string) {
  queryClient.invalidateQueries({ queryKey: ["portfolios", "transactions", portfolioId] });
  queryClient.invalidateQueries({ queryKey: ["portfolios", "summary", portfolioId] });
  queryClient.invalidateQueries({ queryKey: ["portfolios", "holdings", portfolioId] });
  queryClient.invalidateQueries({ queryKey: ["portfolios", "allocation", portfolioId] });
  queryClient.invalidateQueries({ queryKey: ["portfolios", "performance", portfolioId] });
  queryClient.invalidateQueries({ queryKey: ["portfolios", "xirr", portfolioId] });
  queryClient.invalidateQueries({ queryKey: ["net-worth"] });
}

export function useCreateTransaction(portfolioId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: TransactionInput) => portfolioApi.createTransaction(portfolioId, input),
    onSuccess: () => {
      invalidatePortfolioDerivedData(queryClient, portfolioId);
      toast.success("Transaction recorded");
    },
    onError: (error: unknown) => {
      const message = extractErrorMessage(error) ?? "Could not record this transaction";
      toast.error(message);
    },
  });
}

export function useUpdateTransaction(portfolioId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ transactionId, input }: { transactionId: string; input: TransactionInput }) =>
      portfolioApi.updateTransaction(portfolioId, transactionId, input),
    onSuccess: () => {
      invalidatePortfolioDerivedData(queryClient, portfolioId);
      toast.success("Transaction updated");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error) ?? "Could not update this transaction"),
  });
}

export function useDeleteTransaction(portfolioId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (transactionId: string) => portfolioApi.deleteTransaction(portfolioId, transactionId),
    onSuccess: () => {
      invalidatePortfolioDerivedData(queryClient, portfolioId);
      toast.success("Transaction deleted");
    },
    onError: () => toast.error("Could not delete this transaction"),
  });
}

export function useExportPortfolio(portfolioId: string) {
  return {
    exportHoldings: () => portfolioApi.exportHoldings(portfolioId).catch(() => toast.error("Could not export holdings")),
    exportTransactions: () =>
      portfolioApi.exportTransactions(portfolioId).catch(() => toast.error("Could not export transactions")),
    exportSummary: () => portfolioApi.exportSummary(portfolioId).catch(() => toast.error("Could not export the summary")),
  };
}

function extractErrorMessage(error: unknown): string | null {
  if (error && typeof error === "object" && "response" in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    return response?.data?.message ?? null;
  }
  return null;
}
