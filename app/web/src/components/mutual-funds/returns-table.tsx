import { Badge } from "@/components/ui/badge";
import { formatPercent } from "@/lib/utils";
import type { FundReturn } from "@/types/mutual-fund";

const PERIOD_LABELS: Record<string, string> = {
  "1D": "1 Day",
  "1W": "1 Week",
  "1M": "1 Month",
  "3M": "3 Months",
  "6M": "6 Months",
  "1Y": "1 Year",
  "3Y": "3 Years",
  "5Y": "5 Years",
  "10Y": "10 Years",
  SINCE_INCEPTION: "Since Inception",
};

const PERIOD_ORDER = ["1D", "1W", "1M", "3M", "6M", "1Y", "3Y", "5Y", "10Y", "SINCE_INCEPTION"];

export function ReturnsTable({ returns }: { returns: FundReturn[] }) {
  if (returns.length === 0) {
    return <p className="text-sm text-muted-foreground">Returns data is not yet available for this fund.</p>;
  }

  const sorted = [...returns].sort((a, b) => PERIOD_ORDER.indexOf(a.period) - PERIOD_ORDER.indexOf(b.period));

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[480px] text-sm">
        <thead>
          <tr className="border-b border-border text-left text-xs uppercase text-muted-foreground">
            <th className="py-2 pr-4 font-medium">Period</th>
            <th className="py-2 pr-4 font-medium">Return</th>
            <th className="py-2 font-medium">Type</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((r) => {
            const value = Number(r.returnPercentage);
            return (
              <tr key={r.period} className="border-b border-border/60 last:border-0">
                <td className="py-2 pr-4 font-medium">{PERIOD_LABELS[r.period] ?? r.period}</td>
                <td className={`py-2 pr-4 font-semibold ${value >= 0 ? "text-emerald-600 dark:text-emerald-400" : "text-destructive"}`}>
                  {formatPercent(r.returnPercentage)}
                </td>
                <td className="py-2">
                  <Badge variant="outline">{r.annualized ? "Annualized (CAGR)" : "Absolute"}</Badge>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
