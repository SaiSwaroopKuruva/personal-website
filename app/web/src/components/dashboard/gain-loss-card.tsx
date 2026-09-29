"use client";

import { TrendingDown, TrendingUp } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useSelectedPortfolio, usePortfolioSummary } from "@/hooks/use-portfolio";
import { formatInr, formatPercent } from "@/lib/utils";

/** Shows cumulative total gain/loss (realized + unrealized) - not a same-day change, which this app does not track intraday. */
export function GainLossCard() {
  const { selectedPortfolioId, isLoading } = useSelectedPortfolio();
  const summaryQuery = usePortfolioSummary(selectedPortfolioId);
  const gain = summaryQuery.data ? Number(summaryQuery.data.absoluteGainLoss) : null;
  const isPositive = gain !== null && gain >= 0;

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">Total Gain/Loss</CardTitle>
        {isPositive ? <TrendingUp className="h-4 w-4 text-emerald-500" /> : <TrendingDown className="h-4 w-4 text-destructive" />}
      </CardHeader>
      <CardContent>
        {isLoading || summaryQuery.isLoading ? (
          <Skeleton className="h-10 w-full" />
        ) : summaryQuery.data ? (
          <>
            <p className={`text-2xl font-bold ${isPositive ? "text-emerald-600 dark:text-emerald-400" : "text-destructive"}`}>
              {formatInr(summaryQuery.data.absoluteGainLoss)}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              {summaryQuery.data.percentGainLoss !== null ? formatPercent(summaryQuery.data.percentGainLoss) : "—"} realized + unrealized
            </p>
          </>
        ) : (
          <p className="text-sm text-muted-foreground">No portfolio data yet</p>
        )}
      </CardContent>
    </Card>
  );
}
