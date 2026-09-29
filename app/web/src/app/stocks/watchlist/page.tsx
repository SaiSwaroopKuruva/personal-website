"use client";

import Link from "next/link";
import { Star } from "lucide-react";
import { RequireAuth } from "@/components/auth/require-auth";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { ChangeIndicator } from "@/components/ui/change-indicator";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { useStockWatchlist, useToggleWatchlist } from "@/hooks/use-stocks";
import { ROUTES } from "@/lib/constants";
import { formatDate, formatInr } from "@/lib/utils";

function WatchlistContent() {
  const { data: watchlist, isLoading, isError, refetch } = useStockWatchlist();
  const { remove } = useToggleWatchlist();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Stock Watchlist</h1>
        <p className="text-sm text-muted-foreground">Stocks you are tracking for quick access.</p>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-36 w-full" />
          ))}
        </div>
      ) : isError ? (
        <ErrorState title="Could not load your watchlist" onRetry={() => refetch()} />
      ) : !watchlist || watchlist.length === 0 ? (
        <EmptyState
          icon={Star}
          title="Your watchlist is empty"
          description="Browse the stock explorer and tap Watchlist on a stock's details page to track it here."
          action={
            <Button asChild size="sm">
              <Link href={ROUTES.stocks}>Explore Stocks</Link>
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {watchlist.map((item) => (
            <Card key={item.symbol}>
              <CardContent className="space-y-3 pt-6">
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <p className="truncate font-semibold">{item.symbol}</p>
                    <p className="truncate text-xs text-muted-foreground">{item.companyName}</p>
                  </div>
                  <span className="shrink-0 rounded-full bg-secondary px-2 py-0.5 text-[11px]">{item.exchange}</span>
                </div>
                {item.quote ? (
                  <div className="flex items-baseline justify-between">
                    <p className="text-lg font-semibold tabular-nums">{formatInr(item.quote.lastTradedPrice, { decimals: 2 })}</p>
                    <ChangeIndicator changePercent={item.quote.changePercent} />
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">Quote unavailable</p>
                )}
                <p className="text-[11px] text-muted-foreground">Added {formatDate(item.addedAt)}</p>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" className="flex-1" onClick={() => remove.mutate(item.symbol)}>
                    Remove
                  </Button>
                  <Button size="sm" className="flex-1" asChild>
                    <Link href={ROUTES.stockDetails(item.symbol)}>Open Details</Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Disclaimer text="Stock market data may be delayed, subject to provider availability and synchronization intervals. Information displayed is for educational and informational purposes only and should not be considered personalized investment advice." />
    </div>
  );
}

export default function StockWatchlistPage() {
  return (
    <RequireAuth>
      <WatchlistContent />
    </RequireAuth>
  );
}
