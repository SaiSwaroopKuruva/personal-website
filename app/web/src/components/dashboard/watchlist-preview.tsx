"use client";

import Link from "next/link";
import { Star } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ChangeIndicator } from "@/components/ui/change-indicator";
import { useStockWatchlist } from "@/hooks/use-stocks";
import { ROUTES } from "@/lib/constants";
import { formatInr } from "@/lib/utils";

/** Dashboard preview of the current user's stock watchlist (Part 5). */
export function WatchlistPreview() {
  const { data: watchlist, isLoading } = useStockWatchlist();
  const items = (watchlist ?? []).slice(0, 4);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Watchlist</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {isLoading ? (
          <div className="space-y-2">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-6 w-full" />
            ))}
          </div>
        ) : items.length === 0 ? (
          <div className="flex flex-col items-center gap-2 py-2 text-center text-sm text-muted-foreground">
            <Star className="h-6 w-6" />
            <p>No stocks watched yet.</p>
          </div>
        ) : (
          items.map((item) => (
            <div key={item.symbol} className="flex items-center justify-between text-sm">
              <Link href={ROUTES.stockDetails(item.symbol)} className="font-medium hover:underline">
                {item.symbol}
              </Link>
              <div className="flex items-center gap-2">
                <span className="tabular-nums">{formatInr(item.quote?.lastTradedPrice, { decimals: 2 })}</span>
                <ChangeIndicator changePercent={item.quote?.changePercent} showIcon={false} />
              </div>
            </div>
          ))
        )}
        <Link href={ROUTES.stockWatchlist} className="inline-block text-xs text-primary hover:underline">
          View full watchlist →
        </Link>
      </CardContent>
    </Card>
  );
}
