"use client";

import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { TransactionsView } from "@/components/portfolio/transactions-view";
import { useExportPortfolio, useSelectedPortfolio } from "@/hooks/use-portfolio";
import { Download, Wallet } from "lucide-react";

export default function PortfolioTransactionsPage() {
  const { selectedPortfolioId, isLoading } = useSelectedPortfolio();
  const exporters = useExportPortfolio(selectedPortfolioId ?? "");

  if (isLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (!selectedPortfolioId) {
    return <EmptyState icon={Wallet} title="No portfolio selected" description="Create or select a portfolio to view transactions." />;
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button size="sm" variant="outline" onClick={() => exporters.exportTransactions()}>
          <Download className="h-4 w-4" /> Export CSV
        </Button>
      </div>
      <TransactionsView portfolioId={selectedPortfolioId} />
    </div>
  );
}
