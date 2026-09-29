"use client";

import { useState } from "react";
import { Search as SearchIcon, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { ChangeIndicator } from "@/components/ui/change-indicator";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { useStockComparison, useStockSearch } from "@/hooks/use-stocks";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { useStockCompareStore, MAX_COMPARE_STOCKS } from "@/store/stock-compare-store";
import { formatInr } from "@/lib/utils";

export default function StockComparePage() {
  const { symbols, remove, add, isFull, clear } = useStockCompareStore();
  const [searchInput, setSearchInput] = useState("");
  const search = useDebouncedValue(searchInput, 400);

  const { data: comparison, isLoading, isError, refetch } = useStockComparison(symbols);
  const { data: searchResults } = useStockSearch(search);

  return (
    <div className="space-y-6 pb-10">
      <div>
        <h1 className="text-2xl font-bold">Compare Stocks</h1>
        <p className="text-sm text-muted-foreground">Select up to {MAX_COMPARE_STOCKS} stocks to compare factual data side by side.</p>
      </div>

      <Card>
        <CardContent className="space-y-3 pt-6">
          <div className="relative">
            <SearchIcon className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Search stocks to add to comparison..."
              className="pl-9"
              aria-label="Search stocks to compare"
            />
          </div>
          {search && searchResults && (
            <div className="flex flex-wrap gap-2">
              {searchResults.slice(0, 6).map((stock) => (
                <Button
                  key={stock.symbol}
                  variant="outline"
                  size="sm"
                  disabled={symbols.includes(stock.symbol) || isFull()}
                  onClick={() => add(stock.symbol)}
                >
                  {stock.symbol} · {stock.companyName}
                </Button>
              ))}
            </div>
          )}
          <div className="flex flex-wrap items-center gap-2">
            {symbols.map((symbol) => (
              <span key={symbol} className="flex items-center gap-1 rounded-full bg-secondary px-2.5 py-1 text-xs">
                {symbol}
                <button aria-label={`Remove ${symbol}`} onClick={() => remove(symbol)}>
                  <X className="h-3 w-3" />
                </button>
              </span>
            ))}
            {symbols.length > 0 && (
              <Button variant="ghost" size="sm" onClick={clear}>
                Clear all
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {symbols.length < 2 ? (
        <EmptyState title="Select at least 2 stocks" description="Search above and add stocks to see a side-by-side comparison." />
      ) : isLoading ? (
        <Skeleton className="h-80 w-full" />
      ) : isError || !comparison ? (
        <ErrorState title="Could not load comparison" onRetry={() => refetch()} />
      ) : (
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full min-w-[640px] text-sm">
            <thead>
              <tr className="border-b border-border bg-muted/40">
                <th className="p-3 text-left font-medium text-muted-foreground">Metric</th>
                {comparison.stocks.map((stock) => (
                  <th key={stock.symbol} className="p-3 text-left font-semibold">
                    {stock.symbol}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              <tr className="border-b border-border/60">
                <td className="p-3 font-medium text-muted-foreground">Company</td>
                {comparison.stocks.map((s) => (
                  <td key={s.symbol} className="p-3">{s.companyName}</td>
                ))}
              </tr>
              <tr className="border-b border-border/60">
                <td className="p-3 font-medium text-muted-foreground">Exchange</td>
                {comparison.stocks.map((s) => (
                  <td key={s.symbol} className="p-3">{s.exchange}</td>
                ))}
              </tr>
              <tr className="border-b border-border/60">
                <td className="p-3 font-medium text-muted-foreground">Sector</td>
                {comparison.stocks.map((s) => (
                  <td key={s.symbol} className="p-3">{s.sector ?? "—"}</td>
                ))}
              </tr>
              <tr className="border-b border-border/60">
                <td className="p-3 font-medium text-muted-foreground">Series</td>
                {comparison.stocks.map((s) => (
                  <td key={s.symbol} className="p-3">{s.series ?? "—"}</td>
                ))}
              </tr>
              <tr className="border-b border-border/60">
                <td className="p-3 font-medium text-muted-foreground">Lot Size</td>
                {comparison.stocks.map((s) => (
                  <td key={s.symbol} className="p-3">{s.lotSize ?? "—"}</td>
                ))}
              </tr>
              <tr className="border-b border-border/60">
                <td className="p-3 font-medium text-muted-foreground">Last Traded Price</td>
                {comparison.stocks.map((s) => (
                  <td key={s.symbol} className="p-3 tabular-nums">{formatInr(s.quote?.lastTradedPrice, { decimals: 2 })}</td>
                ))}
              </tr>
              <tr className="border-b border-border/60 last:border-0">
                <td className="p-3 font-medium text-muted-foreground">Day Change</td>
                {comparison.stocks.map((s) => (
                  <td key={s.symbol} className="p-3">
                    {s.quote ? <ChangeIndicator changePercent={s.quote.changePercent} /> : "—"}
                  </td>
                ))}
              </tr>
            </tbody>
          </table>
          <p className="border-t border-border bg-muted/20 p-3 text-xs text-muted-foreground">{comparison.dataLimitationNote}</p>
        </div>
      )}

      <Disclaimer text={comparison?.disclaimer} />
    </div>
  );
}
