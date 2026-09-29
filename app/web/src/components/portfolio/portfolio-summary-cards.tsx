import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatInr, formatPercent } from "@/lib/utils";
import type { PortfolioSummary } from "@/types/portfolio";

function formatShare(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === "") return "—";
  const numeric = typeof value === "string" ? Number(value) : value;
  if (Number.isNaN(numeric)) return "—";
  return `${numeric.toFixed(0)}%`;
}

/**
 * Portfolio overview metric cards (Part 8/13). Deliberately never merges XIRR with cumulative gain -
 * they are shown as two separate, clearly labelled cards.
 */
export function PortfolioSummaryCards({ summary }: { summary: PortfolioSummary }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Invested</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold">{formatInr(summary.investedAmount)}</p>
          <p className="mt-1 text-xs text-muted-foreground">Cost basis of current holdings</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Current Value</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold">
            {summary.currentMarketValue !== null ? formatInr(summary.currentMarketValue) : "Unavailable"}
          </p>
          <p className="mt-1 text-xs text-muted-foreground">
            {summary.partialValuation ? "Some holdings excluded - see note below" : "Sum of latest valid valuations"}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Total Gain/Loss</CardTitle>
        </CardHeader>
        <CardContent>
          <p className={`text-2xl font-bold ${Number(summary.absoluteGainLoss) >= 0 ? "text-emerald-600 dark:text-emerald-400" : "text-destructive"}`}>
            {formatInr(summary.absoluteGainLoss)}
          </p>
          <p className="mt-1 text-xs text-muted-foreground">
            {summary.percentGainLoss !== null ? formatPercent(summary.percentGainLoss) : "—"} · realized + unrealized
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">XIRR</CardTitle>
        </CardHeader>
        <CardContent>
          {summary.xirr?.status === "CALCULATED" ? (
            <p className="text-2xl font-bold">{formatPercent(summary.xirr.xirrPercent)}</p>
          ) : (
            <p className="text-lg font-semibold text-muted-foreground">Not available</p>
          )}
          <p className="mt-1 text-xs text-muted-foreground">
            {summary.xirr?.status === "CALCULATED"
              ? "Annualized, money-weighted return"
              : summary.xirr?.message ?? "XIRR requires dated cash flows"}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Holdings</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold">{summary.holdingsCount}</p>
          <p className="mt-1 text-xs text-muted-foreground">
            Stocks {formatShare(summary.stockAllocationPercent)} · Funds {formatShare(summary.mutualFundAllocationPercent)}
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
