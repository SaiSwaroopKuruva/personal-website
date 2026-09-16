import { Wallet } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

// Placeholder data — real portfolio integration is planned for a later phase.
const PORTFOLIO = {
  totalValue: "₹12,45,320",
  invested: "₹10,20,000",
  returns: "+₹2,25,320",
  returnsPercent: "+22.1%",
};

export function PortfolioSummaryCard() {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">Portfolio Summary</CardTitle>
        <Wallet className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        <p className="text-2xl font-bold">{PORTFOLIO.totalValue}</p>
        <p className="mt-1 text-xs text-muted-foreground">
          Invested {PORTFOLIO.invested} · <span className="text-emerald-600 dark:text-emerald-400">{PORTFOLIO.returns} ({PORTFOLIO.returnsPercent})</span>
        </p>
      </CardContent>
    </Card>
  );
}
