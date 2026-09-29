"use client";

import Link from "next/link";
import { Wallet } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useSelectedPortfolio, usePortfolioSummary } from "@/hooks/use-portfolio";
import { ROUTES } from "@/lib/constants";
import { formatInr, formatPercent } from "@/lib/utils";

export function PortfolioSummaryCard() {
  const { portfolios, selectedPortfolioId, isLoading } = useSelectedPortfolio();
  const summaryQuery = usePortfolioSummary(selectedPortfolioId);

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">Portfolio Summary</CardTitle>
        <Wallet className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        {isLoading || summaryQuery.isLoading ? (
          <Skeleton className="h-10 w-full" />
        ) : portfolios.length === 0 ? (
          <div>
            <p className="text-sm text-muted-foreground">No portfolio yet</p>
            <Link href={ROUTES.portfolio} className="text-xs text-primary hover:underline">
              Create your first portfolio
            </Link>
          </div>
        ) : summaryQuery.data ? (
          <Link href={ROUTES.portfolio}>
            <p className="text-2xl font-bold">
              {summaryQuery.data.currentMarketValue !== null ? formatInr(summaryQuery.data.currentMarketValue) : "Unavailable"}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              Invested {formatInr(summaryQuery.data.investedAmount)} ·{" "}
              <span className={Number(summaryQuery.data.absoluteGainLoss) >= 0 ? "text-emerald-600 dark:text-emerald-400" : "text-destructive"}>
                {formatInr(summaryQuery.data.absoluteGainLoss)} ({summaryQuery.data.percentGainLoss !== null ? formatPercent(summaryQuery.data.percentGainLoss) : "—"})
              </span>
            </p>
          </Link>
        ) : null}
      </CardContent>
    </Card>
  );
}

