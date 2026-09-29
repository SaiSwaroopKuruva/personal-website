import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatInr } from "@/lib/utils";
import type { NetWorthSummary } from "@/types/net-worth";

export function NetWorthSummaryCards({ summary }: { summary: NetWorthSummary }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Net Worth</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold">{formatInr(summary.netWorth)}</p>
          <p className="mt-1 text-xs text-muted-foreground">Total assets minus liabilities</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Total Assets</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold">{formatInr(summary.totalAssets)}</p>
          <p className="mt-1 text-xs text-muted-foreground">
            Investments {formatInr(summary.investmentAssets)} · Other {formatInr(summary.nonInvestmentAssets)}
          </p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Total Liabilities</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold text-destructive">{formatInr(summary.totalLiabilities)}</p>
          <p className="mt-1 text-xs text-muted-foreground">Loans, credit cards and other tracked debts</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Investment Assets</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold">{formatInr(summary.investmentAssets)}</p>
          <p className="mt-1 text-xs text-muted-foreground">
            {summary.partialInvestmentValuation ? "Some holdings could not be valued" : "From your tracked portfolios"}
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
