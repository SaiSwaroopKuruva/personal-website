"use client";

import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ChangeIndicator } from "@/components/ui/change-indicator";
import { ROUTES } from "@/lib/constants";
import { useMarketIndices } from "@/hooks/use-market";
import { formatInr } from "@/lib/utils";

/** Real, provider-backed market indices (Part 5/39) - replaces the earlier hard-coded placeholder values. */
export function MarketSnapshot() {
  const { data: indices, isLoading, isError } = useMarketIndices();

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Market Snapshot</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {isLoading ? (
          <div className="space-y-2">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-6 w-full" />
            ))}
          </div>
        ) : isError || !indices || indices.length === 0 ? (
          <p className="text-sm text-muted-foreground">Market data unavailable right now.</p>
        ) : (
          indices.map((index) => (
            <div key={index.name} className="flex items-center justify-between text-sm">
              <span className="font-medium">{index.name}</span>
              <div className="flex items-center gap-2">
                <span className="tabular-nums">{formatInr(index.lastPrice, { decimals: 2 })}</span>
                <ChangeIndicator changePercent={index.changePercent} showIcon={false} />
              </div>
            </div>
          ))
        )}
        <Link href={ROUTES.market} className="inline-block text-xs text-primary hover:underline">
          View full market page →
        </Link>
      </CardContent>
    </Card>
  );
}
