"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { AllocationBreakdown } from "@/components/portfolio/allocation-breakdown";
import { usePortfolioAllocation, useSelectedPortfolio } from "@/hooks/use-portfolio";
import { Wallet } from "lucide-react";

export default function PortfolioAllocationPage() {
  const { selectedPortfolioId, isLoading } = useSelectedPortfolio();
  const allocationQuery = usePortfolioAllocation(selectedPortfolioId);

  if (isLoading || allocationQuery.isLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (!selectedPortfolioId) {
    return <EmptyState icon={Wallet} title="No portfolio selected" description="Create or select a portfolio to view allocation." />;
  }

  if (allocationQuery.isError || !allocationQuery.data) {
    return <ErrorState onRetry={() => allocationQuery.refetch()} />;
  }

  const allocation = allocationQuery.data;

  return (
    <div className="space-y-6">
      {allocation.partialValuation ? (
        <div className="rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-900 dark:bg-amber-950/40 dark:text-amber-300">
          {allocation.partialValuationMessage}
        </div>
      ) : null}
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">By Asset Class</CardTitle>
          </CardHeader>
          <CardContent>
            <AllocationBreakdown title="asset classes" slices={allocation.byAssetClass} />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-base">By Holding</CardTitle>
          </CardHeader>
          <CardContent>
            <AllocationBreakdown title="holdings" slices={allocation.byHolding} />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
