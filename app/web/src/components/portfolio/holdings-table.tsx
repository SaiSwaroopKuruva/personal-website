"use client";

import { useMemo, useState } from "react";
import { FreshnessBadge } from "@/components/portfolio/freshness-badge";
import { Badge } from "@/components/ui/badge";
import { Select } from "@/components/ui/select";
import { EmptyState } from "@/components/ui/empty-state";
import { formatDate, formatInr, formatPercent } from "@/lib/utils";
import type { Holding } from "@/types/portfolio";
import { Wallet } from "lucide-react";

type AssetFilter = "ALL" | "STOCK" | "MUTUAL_FUND";
type SortKey = "marketValue" | "costBasis" | "gainLoss" | "gainLossPercent" | "name";

interface HoldingsTableProps {
  holdings: Holding[];
}

export function HoldingsTable({ holdings }: HoldingsTableProps) {
  const [assetFilter, setAssetFilter] = useState<AssetFilter>("ALL");
  const [sortKey, setSortKey] = useState<SortKey>("marketValue");

  const filtered = useMemo(() => {
    const byFilter = assetFilter === "ALL" ? holdings : holdings.filter((h) => h.assetType === assetFilter);
    return [...byFilter].sort((a, b) => {
      switch (sortKey) {
        case "costBasis":
          return Number(b.costBasis) - Number(a.costBasis);
        case "gainLoss":
          return Number(b.unrealizedGain ?? -Infinity) - Number(a.unrealizedGain ?? -Infinity);
        case "gainLossPercent":
          return Number(b.unrealizedGainPercent ?? -Infinity) - Number(a.unrealizedGainPercent ?? -Infinity);
        case "name":
          return a.name.localeCompare(b.name);
        case "marketValue":
        default:
          return Number(b.marketValue ?? -Infinity) - Number(a.marketValue ?? -Infinity);
      }
    });
  }, [holdings, assetFilter, sortKey]);

  if (holdings.length === 0) {
    return (
      <EmptyState
        icon={Wallet}
        title="No holdings yet"
        description="Add your first holding to start tracking your portfolio."
      />
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-3">
        <Select value={assetFilter} onChange={(e) => setAssetFilter(e.target.value as AssetFilter)} className="w-auto" aria-label="Filter by asset type">
          <option value="ALL">All assets</option>
          <option value="STOCK">Stocks</option>
          <option value="MUTUAL_FUND">Mutual funds</option>
        </Select>
        <Select value={sortKey} onChange={(e) => setSortKey(e.target.value as SortKey)} className="w-auto" aria-label="Sort holdings">
          <option value="marketValue">Sort: Market value</option>
          <option value="costBasis">Sort: Cost basis</option>
          <option value="gainLoss">Sort: Gain/Loss</option>
          <option value="gainLossPercent">Sort: % Gain/Loss</option>
          <option value="name">Sort: Name</option>
        </Select>
      </div>

      {/* Desktop table */}
      <div className="hidden overflow-x-auto rounded-lg border border-border md:block">
        <table className="w-full text-sm">
          <thead className="bg-muted/40 text-left text-xs uppercase text-muted-foreground">
            <tr>
              <th className="px-4 py-3">Asset</th>
              <th className="px-4 py-3">Units</th>
              <th className="px-4 py-3">Avg Cost</th>
              <th className="px-4 py-3">Cost Basis</th>
              <th className="px-4 py-3">Latest Price</th>
              <th className="px-4 py-3">Market Value</th>
              <th className="px-4 py-3">Gain/Loss</th>
              <th className="px-4 py-3">Allocation</th>
              <th className="px-4 py-3">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {filtered.map((holding) => (
              <tr key={`${holding.assetType}-${holding.symbol}`}>
                <td className="px-4 py-3">
                  <p className="font-medium">{holding.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {holding.symbol} <Badge variant="outline" className="ml-1">{holding.assetType === "STOCK" ? "Stock" : "Mutual Fund"}</Badge>
                  </p>
                </td>
                <td className="px-4 py-3">{holding.quantity}</td>
                <td className="px-4 py-3">{formatInr(holding.averageCost, { decimals: 2 })}</td>
                <td className="px-4 py-3">{formatInr(holding.costBasis)}</td>
                <td className="px-4 py-3">
                  {holding.valuation?.price != null ? formatInr(holding.valuation.price, { decimals: 2 }) : "—"}
                  {holding.valuation?.sourceTimestamp ? (
                    <p className="text-xs text-muted-foreground">as of {formatDate(holding.valuation.sourceTimestamp)}</p>
                  ) : null}
                </td>
                <td className="px-4 py-3">{holding.marketValue !== null ? formatInr(holding.marketValue) : "Unavailable"}</td>
                <td className="px-4 py-3">
                  {holding.unrealizedGain !== null ? (
                    <span className={Number(holding.unrealizedGain) >= 0 ? "text-emerald-600 dark:text-emerald-400" : "text-destructive"}>
                      {formatInr(holding.unrealizedGain)} ({formatPercent(holding.unrealizedGainPercent)})
                    </span>
                  ) : (
                    "—"
                  )}
                </td>
                <td className="px-4 py-3">{holding.allocationPercent !== null ? `${Number(holding.allocationPercent).toFixed(1)}%` : "—"}</td>
                <td className="px-4 py-3">
                  <FreshnessBadge status={holding.valuation?.status ?? "UNAVAILABLE"} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile cards */}
      <div className="space-y-3 md:hidden">
        {filtered.map((holding) => (
          <div key={`${holding.assetType}-${holding.symbol}`} className="rounded-lg border border-border p-4">
            <div className="flex items-start justify-between">
              <div>
                <p className="font-medium">{holding.name}</p>
                <p className="text-xs text-muted-foreground">{holding.symbol}</p>
              </div>
              <FreshnessBadge status={holding.valuation?.status ?? "UNAVAILABLE"} />
            </div>
            <div className="mt-3 grid grid-cols-2 gap-2 text-sm">
              <span className="text-muted-foreground">Units</span>
              <span className="text-right">{holding.quantity}</span>
              <span className="text-muted-foreground">Cost basis</span>
              <span className="text-right">{formatInr(holding.costBasis)}</span>
              <span className="text-muted-foreground">Market value</span>
              <span className="text-right">{holding.marketValue !== null ? formatInr(holding.marketValue) : "Unavailable"}</span>
              <span className="text-muted-foreground">Gain/Loss</span>
              <span className={`text-right ${holding.unrealizedGain !== null && Number(holding.unrealizedGain) >= 0 ? "text-emerald-600 dark:text-emerald-400" : "text-destructive"}`}>
                {holding.unrealizedGain !== null ? formatInr(holding.unrealizedGain) : "—"}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
