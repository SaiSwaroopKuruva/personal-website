import { DataSourceNote } from "@/components/stocks/data-source-note";
import { ChangeIndicator } from "@/components/ui/change-indicator";
import { formatInr } from "@/lib/utils";
import type { StockQuote } from "@/types/stock";

export function StockQuotePanel({ quote }: { quote: StockQuote }) {
  return (
    <div className="space-y-3 rounded-xl border border-border/60 bg-card p-5">
      <div className="flex items-baseline justify-between gap-4">
        <div>
          <p className="text-3xl font-bold tabular-nums">{formatInr(quote.lastTradedPrice, { decimals: 2 })}</p>
          <p className="flex items-center gap-2 text-sm">
            <span className={Number(quote.change ?? 0) >= 0 ? "text-positive" : "text-negative"}>
              {formatInr(quote.change, { decimals: 2 })}
            </span>
            <ChangeIndicator changePercent={quote.changePercent} />
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
