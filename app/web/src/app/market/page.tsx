"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { MoversTable } from "@/components/stocks/movers-table";
import { ProviderStatusBanner } from "@/components/stocks/provider-status-banner";
import { useMarketGainers, useMarketIndices, useMarketLosers, useMarketMostActive, useMarketStatus } from "@/hooks/use-market";
import { formatPercent } from "@/lib/utils";

const STOCK_DISCLAIMER =
  "Market data may be delayed, subject to provider availability, data-source limitations and synchronization intervals. Gainers/losers/most-active are computed from real quotes across this platform's tracked stock universe, not a full-exchange screener. This is informational data only and does not constitute investment advice.";

export default function MarketOverviewPage() {
  const { data: indices, isLoading: indicesLoading } = useMarketIndices();
  const { data: status } = useMarketStatus("NSE");
  const { data: gainers, isLoading: gainersLoading } = useMarketGainers();
  const { data: losers, isLoading: losersLoading } = useMarketLosers();
  const { data: mostActive, isLoading: activeLoading } = useMarketMostActive();

  return (
    <div className="space-y-6 pb-20">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Market Overview</h1>
          <p className="text-sm text-muted-foreground">
            NSE status: <span className="font-medium text-foreground">{status?.status ?? "—"}</span>
          </p>
        </div>
        <ProviderStatusBanner />
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {indicesLoading
          ? Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-24 w-full" />)
          : indices?.map((index) => (
              <Card key={index.name}>
                <CardContent className="pt-6">
                  <p className="text-sm text-muted-foreground">{index.name.replace(/^NSE_INDEX:/, "")}</p>
                  <p className="text-xl font-bold">{index.lastPrice ?? "—"}</p>
                  <p className={`text-sm font-medium ${Number(index.changePercent ?? 0) >= 0 ? "text-emerald-600" : "text-destructive"}`}>
                    {formatPercent(index.changePercent)}
                  </p>
                </CardContent>
              </Card>
            ))}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>Top Gainers</CardTitle>
          </CardHeader>
          <CardContent>{gainersLoading ? <Skeleton className="h-40 w-full" /> : <MoversTable quotes={gainers ?? []} />}</CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Top Losers</CardTitle>
          </CardHeader>
          <CardContent>{losersLoading ? <Skeleton className="h-40 w-full" /> : <MoversTable quotes={losers ?? []} />}</CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Most Active (Volume)</CardTitle>
          </CardHeader>
          <CardContent>
            {activeLoading ? <Skeleton className="h-40 w-full" /> : <MoversTable quotes={mostActive ?? []} valueLabel="LTP" />}
          </CardContent>
        </Card>
      </div>

      <Disclaimer text={STOCK_DISCLAIMER} />
    </div>
  );
}
