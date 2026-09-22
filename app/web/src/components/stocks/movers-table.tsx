import Link from "next/link";
import { ROUTES } from "@/lib/constants";
import { formatInr, formatPercent } from "@/lib/utils";
import type { StockQuote } from "@/types/stock";

export function MoversTable({ quotes, valueLabel = "LTP" }: { quotes: StockQuote[]; valueLabel?: string }) {
  if (quotes.length === 0) {
    return <p className="text-sm text-muted-foreground">No data available.</p>;
  }
  return (
    <table className="w-full text-sm">
      <thead className="text-left text-xs uppercase text-muted-foreground">
        <tr>
          <th className="pb-2">Symbol</th>
          <th className="pb-2 text-right">{valueLabel}</th>
          <th className="pb-2 text-right">Change %</th>
        </tr>
      </thead>
      <tbody>
        {quotes.map((q) => (
          <tr key={q.symbol} className="border-t border-border/60">
            <td className="py-2">
              <Link href={ROUTES.stockDetails(q.symbol)} className="font-medium hover:underline">
                {q.symbol}
              </Link>
            </td>
            <td className="py-2 text-right">{formatInr(q.lastTradedPrice, { decimals: 2 })}</td>
            <td className={`py-2 text-right font-medium ${Number(q.changePercent ?? 0) >= 0 ? "text-emerald-600" : "text-destructive"}`}>
              {formatPercent(q.changePercent)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
