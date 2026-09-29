"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { PerformanceChart } from "@/components/portfolio/performance-chart";
import { usePortfolioPerformance, usePortfolioXirr, useSelectedPortfolio } from "@/hooks/use-portfolio";
import { formatPercent } from "@/lib/utils";
import { cn } from "@/lib/utils";
import { TrendingUp, Wallet } from "lucide-react";
import type { PerformanceRange } from "@/types/portfolio";

const RANGES: PerformanceRange[] = ["1M", "3M", "6M", "1Y", "3Y", "5Y", "ALL"];

export default function PortfolioPerformancePage() {
  const { selectedPortfolioId, isLoading } = useSelectedPortfolio();
  const [range, setRange] = useState<PerformanceRange>("1Y");
  const performanceQuery = usePortfolioPerformance(selectedPortfolioId, range);
  const xirrQuery = usePortfolioXirr(selectedPortfolioId);

  if (isLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (!selectedPortfolioId) {
    return <EmptyState icon={Wallet} title="No portfolio selected" description="Create or select a portfolio to view performance." />;
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="flex flex-row flex-wrap items-center justify-between gap-3 space-y-0">
          <CardTitle className="text-base">Portfolio Performance</CardTitle>
          <div className="flex gap-1">
            {RANGES.map((r) => (
              <Button
                key={r}
                size="sm"
                variant={range === r ? "default" : "outline"}
                onClick={() => setRange(r)}
                className={cn("px-2.5")}
              >
                {r}
              </Button>
            ))}
          </div>
        </CardHeader>
        <CardContent>
          {performanceQuery.isLoading ? (
            <Skeleton className="h-48 w-full" />
          ) : performanceQuery.data && performanceQuery.data.points.length > 0 ? (
            <>
              <PerformanceChart points={performanceQuery.data.points} />
              {performanceQuery.data.limitedHistory ? (
                <p className="mt-2 text-xs text-muted-foreground">{performanceQuery.data.limitationMessage}</p>
              ) : null}
            </>
          ) : (
            <EmptyState
              icon={TrendingUp}
              title="No performance history yet"
              description={performanceQuery.data?.limitationMessage ?? "History builds up as daily portfolio valuation snapshots accumulate."}
            />
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Investment Return (XIRR)</CardTitle>
        </CardHeader>
        <CardContent>
          {xirrQuery.data?.status === "CALCULATED" ? (
            <div>
              <p className="text-3xl font-bold">{formatPercent(xirrQuery.data.xirrPercent)}</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Annualized, money-weighted return based on your dated cash flows. Not a guarantee of future performance.
              </p>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">
              {xirrQuery.data?.message ?? "XIRR cannot be calculated from the available cash flows."}
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
