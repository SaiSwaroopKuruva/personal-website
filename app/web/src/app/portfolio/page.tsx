"use client";

import Link from "next/link";
import { ListChecks, PlusCircle, Receipt, TrendingUp } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { PortfolioSummaryCards } from "@/components/portfolio/portfolio-summary-cards";
import { PerformanceChart } from "@/components/portfolio/performance-chart";
import { AllocationBreakdown } from "@/components/portfolio/allocation-breakdown";
import { TransactionFormDialog } from "@/components/portfolio/transaction-form-dialog";
import { useSelectedPortfolio, usePortfolioAllocation, usePortfolioHoldings, usePortfolioPerformance, usePortfolioSummary } from "@/hooks/use-portfolio";
import { formatDate } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { useState } from "react";

export default function PortfolioOverviewPage() {
  const { portfolios, selectedPortfolioId, isLoading: portfoliosLoading } = useSelectedPortfolio();
  const summaryQuery = usePortfolioSummary(selectedPortfolioId);
  const holdingsQuery = usePortfolioHoldings(selectedPortfolioId);
  const performanceQuery = usePortfolioPerformance(selectedPortfolioId, "1Y");
  const allocationQuery = usePortfolioAllocation(selectedPortfolioId);
  const [txDialogOpen, setTxDialogOpen] = useState(false);

  if (portfoliosLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (portfolios.length === 0) {
    return (
      <EmptyState
        icon={TrendingUp}
        title="Create your first portfolio"
        description="Portfolios group your holdings and transactions. Use the New button above to get started."
      />
    );
  }

  if (summaryQuery.isLoading || !selectedPortfolioId) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (summaryQuery.isError || !summaryQuery.data) {
    return <ErrorState onRetry={() => summaryQuery.refetch()} />;
  }

  const summary = summaryQuery.data;
  const hasHoldings = (holdingsQuery.data?.holdings.length ?? 0) > 0;

  return (
    <div className="space-y-6">
      <PortfolioSummaryCards summary={summary} />

      {summary.partialValuation ? (
        <div className="rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-900 dark:bg-amber-950/40 dark:text-amber-300">
          {summary.partialValuationMessage}
        </div>
      ) : null}

      <div className="flex flex-wrap gap-2">
        <Button size="sm" onClick={() => setTxDialogOpen(true)}>
          <PlusCircle className="h-4 w-4" /> Record transaction
        </Button>
        <Button size="sm" variant="outline" asChild>
          <Link href={ROUTES.portfolioHoldings}>
            <ListChecks className="h-4 w-4" /> View holdings
          </Link>
        </Button>
        <Button size="sm" variant="outline" asChild>
          <Link href={ROUTES.portfolioTransactions}>
            <Receipt className="h-4 w-4" /> View transactions
          </Link>
        </Button>
      </div>

      {!hasHoldings ? (
        <EmptyState
          icon={TrendingUp}
          title="Add your first holding to start tracking your portfolio."
          description="Record a buy/purchase transaction (including past investments) to see valuation, gains and allocation here."
          action={
            <Button size="sm" onClick={() => setTxDialogOpen(true)}>
              <PlusCircle className="h-4 w-4" /> Record transaction
            </Button>
          }
        />
      ) : (
        <div className="grid gap-6 lg:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Performance (1Y)</CardTitle>
            </CardHeader>
            <CardContent>
              {performanceQuery.data ? (
                <>
                  <PerformanceChart points={performanceQuery.data.points} />
                  {performanceQuery.data.limitedHistory ? (
                    <p className="mt-2 text-xs text-muted-foreground">{performanceQuery.data.limitationMessage}</p>
                  ) : null}
                </>
              ) : (
                <Skeleton className="h-48 w-full" />
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Asset Allocation</CardTitle>
            </CardHeader>
            <CardContent>
              {allocationQuery.data ? (
                <AllocationBreakdown title="allocation" slices={allocationQuery.data.byAssetClass} />
              ) : (
                <Skeleton className="h-48 w-full" />
              )}
            </CardContent>
          </Card>
        </div>
      )}

      <p className="text-xs text-muted-foreground">
        {summary.lastValuationAt ? `Last valuation: ${formatDate(summary.lastValuationAt)}` : "No valuation yet"}
      </p>

      <TransactionFormDialog portfolioId={selectedPortfolioId} open={txDialogOpen} onOpenChange={setTxDialogOpen} />
    </div>
  );
}
