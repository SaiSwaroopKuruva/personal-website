"use client";

import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { HoldingsTable } from "@/components/portfolio/holdings-table";
import { useExportPortfolio, usePortfolioHoldings, useSelectedPortfolio } from "@/hooks/use-portfolio";
import { Button } from "@/components/ui/button";
import { Download, Wallet } from "lucide-react";

export default function PortfolioHoldingsPage() {
  const { selectedPortfolioId, isLoading: portfoliosLoading } = useSelectedPortfolio();
  const holdingsQuery = usePortfolioHoldings(selectedPortfolioId);
  const exporters = useExportPortfolio(selectedPortfolioId ?? "");

  if (portfoliosLoading || holdingsQuery.isLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (!selectedPortfolioId) {
    return <EmptyState icon={Wallet} title="No portfolio selected" description="Create or select a portfolio to view holdings." />;
  }

  if (holdingsQuery.isError || !holdingsQuery.data) {
    return <ErrorState onRetry={() => holdingsQuery.refetch()} />;
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button size="sm" variant="outline" onClick={() => exporters.exportHoldings()}>
          <Download className="h-4 w-4" /> Export CSV
        </Button>
      </div>
      {holdingsQuery.data.partialValuation ? (
        <div className="rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-900 dark:bg-amber-950/40 dark:text-amber-300">
          {holdingsQuery.data.partialValuationMessage}
        </div>
      ) : null}
      <HoldingsTable holdings={holdingsQuery.data.holdings} />
    </div>
  );
}
