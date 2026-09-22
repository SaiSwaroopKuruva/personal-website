"use client";

import { useState } from "react";
import { Search as SearchIcon } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { StockCard } from "@/components/stocks/stock-card";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { ProviderStatusBanner } from "@/components/stocks/provider-status-banner";
import { useStockSearch } from "@/hooks/use-stocks";
import { useDebouncedValue } from "@/hooks/use-debounced-value";

const STOCK_DISCLAIMER =
  "Stock market data may be delayed, subject to provider availability, data-source limitations and synchronization intervals. This platform provides informational data only and does not constitute investment advice. This platform does not support placing trades or orders.";

export default function StocksExplorerPage() {
  const [searchInput, setSearchInput] = useState("");
  const query = useDebouncedValue(searchInput, 400);
  const { data, isLoading, isError, refetch } = useStockSearch(query);

  return (
    <div className="space-y-6 pb-20">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Stocks</h1>
          <p className="text-sm text-muted-foreground">Search NSE-listed stocks for provider-backed quotes and history.</p>
        </div>
        <ProviderStatusBanner />
      </div>

      <div className="relative max-w-lg">
        <SearchIcon className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          placeholder="Search by symbol or company name (e.g. RELIANCE)"
          className="pl-9"
          aria-label="Search stocks"
        />
      </div>

      {query.trim().length === 0 ? (
        <EmptyState icon={SearchIcon} title="Search for a stock" description="Type a symbol or company name above to get started." />
      ) : isLoading ? (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-full" />
          ))}
        </div>
      ) : isError ? (
        <ErrorState title="Could not search stocks" description="Please check your connection and try again." onRetry={() => refetch()} />
      ) : !data || data.length === 0 ? (
        <EmptyState icon={SearchIcon} title="No stocks found" description="Try a different symbol or company name." />
      ) : (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {data.map((stock) => (
            <StockCard key={stock.symbol} stock={stock} />
          ))}
        </div>
      )}

      <Disclaimer text={STOCK_DISCLAIMER} />
    </div>
  );
}
