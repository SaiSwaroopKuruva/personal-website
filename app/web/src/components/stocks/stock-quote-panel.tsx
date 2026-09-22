import { ArrowDown, ArrowUp } from "lucide-react";
import { DataSourceNote } from "@/components/stocks/data-source-note";
import { formatInr, formatPercent } from "@/lib/utils";
import type { StockQuote } from "@/types/stock";

export function StockQuotePanel({ quote }: { quote: StockQuote }) {
  const changeValue = Number(quote.changePercent ?? 0);
  const isUp = changeValue >= 0;

  return (
    <div className="space-y-3 rounded-xl border border-border/60 bg-card p-5">
      <div className="flex items-baseline justify-between gap-4">
        <div>
          <p className="text-3xl font-bold">{formatInr(quote.lastTradedPrice, { decimals: 2 })}</p>
          <p className={`flex items-center gap-1 text-sm font-medium ${isUp ? "text-emerald-600" : "text-destructive"}`}>
            {isUp ? <ArrowUp className="h-4 w-4" /> : <ArrowDown className="h-4 w-4" />}
            {formatInr(quote.change, { decimals: 2 })} ({formatPercent(quote.changePercent)})
          </p>
        </div>
        <dl className="grid grid-cols-2 gap-x-4 gap-y-1 text-right text-xs text-muted-foreground">
          <dt>Open</dt>
          <dd className="text-foreground">{formatInr(quote.open, { decimals: 2 })}</dd>
          <dt>High</dt>
          <dd className="text-foreground">{formatInr(quote.high, { decimals: 2 })}</dd>
          <dt>Low</dt>
          <dd className="text-foreground">{formatInr(quote.low, { decimals: 2 })}</dd>
          <dt>Prev. Close</dt>
          <dd className="text-foreground">{formatInr(quote.previousClose, { decimals: 2 })}</dd>
        </dl>
      </div>
      <DataSourceNote source={quote.source} dataType={quote.dataType} timestamp={quote.timestamp} freshness={quote.freshness} />
    </div>
  );
}
