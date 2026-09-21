"use client";

import { useState } from "react";
import { Search as SearchIcon, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { useMutualFundComparison, useMutualFundSearch } from "@/hooks/use-mutual-funds";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { useCompareStore, MAX_COMPARE_FUNDS } from "@/store/compare-store";
import { formatCrores, formatNav, formatPercent } from "@/lib/utils";
import type { ComparisonFund } from "@/types/mutual-fund";

const ROWS: { label: string; key: keyof ComparisonFund }[] = [
  { label: "AMC", key: "amcName" },
  { label: "Category", key: "category" },
  { label: "Risk Level", key: "riskLevel" },
  { label: "NAV", key: "nav" },
  { label: "AUM", key: "aum" },
  { label: "Expense Ratio", key: "expenseRatio" },
  { label: "1Y Return", key: "oneYearReturn" },
  { label: "3Y Return", key: "threeYearReturn" },
  { label: "5Y Return", key: "fiveYearReturn" },
  { label: "Since Inception", key: "sinceInceptionReturn" },
  { label: "Min. SIP", key: "minimumSip" },
  { label: "Min. Lumpsum", key: "minimumLumpsum" },
  { label: "Exit Load", key: "exitLoad" },
  { label: "Benchmark", key: "benchmark" },
  { label: "Fund Manager", key: "fundManager" },
];

function formatCell(key: string, value: unknown): string {
  if (value === null || value === undefined) return "—";
  if (key === "nav") return formatNav(value as string);
  if (key === "aum") return formatCrores(value as string);
  if (key === "expenseRatio") return `${value}%`;
  if (["oneYearReturn", "threeYearReturn", "fiveYearReturn", "sinceInceptionReturn"].includes(key)) {
    return formatPercent(value as string);
  }
  if (key === "riskLevel") return String(value).replace(/_/g, " ");
  if (["minimumSip", "minimumLumpsum"].includes(key)) return `₹${new Intl.NumberFormat("en-IN").format(Number(value))}`;
  return String(value);
}

export default function MutualFundComparePage() {
  const { schemeCodes, remove, add, isFull, clear } = useCompareStore();
  const [searchInput, setSearchInput] = useState("");
  const search = useDebouncedValue(searchInput, 400);

  const { data: comparison, isLoading, isError, refetch } = useMutualFundComparison(schemeCodes);
  const { data: searchResults } = useMutualFundSearch({ search: search || undefined, size: 6 });

  return (
    <div className="space-y-6 pb-10">
      <div>
        <h1 className="text-2xl font-bold">Compare Mutual Funds</h1>
        <p className="text-sm text-muted-foreground">Select up to {MAX_COMPARE_FUNDS} funds to compare factual metrics side by side.</p>
      </div>

      <Card>
        <CardContent className="space-y-3 pt-6">
          <div className="relative">
            <SearchIcon className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Search funds to add to comparison..."
              className="pl-9"
              aria-label="Search funds to compare"
            />
          </div>
          {search && searchResults && (
            <div className="flex flex-wrap gap-2">
              {searchResults.content.map((fund) => (
                <Button
                  key={fund.schemeCode}
                  variant="outline"
                  size="sm"
                  disabled={schemeCodes.includes(fund.schemeCode) || isFull()}
                  onClick={() => add(fund.schemeCode)}
                >
                  {fund.schemeName}
                </Button>
              ))}
            </div>
          )}
          <div className="flex flex-wrap items-center gap-2">
            {schemeCodes.map((code) => (
              <span key={code} className="flex items-center gap-1 rounded-full bg-secondary px-2.5 py-1 text-xs">
                {code}
                <button aria-label={`Remove ${code}`} onClick={() => remove(code)}>
                  <X className="h-3 w-3" />
                </button>
              </span>
            ))}
            {schemeCodes.length > 0 && (
              <Button variant="ghost" size="sm" onClick={clear}>
                Clear all
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {schemeCodes.length < 2 ? (
        <EmptyState title="Select at least 2 funds" description="Search above and add funds to see a side-by-side comparison." />
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
                {comparison.funds.map((fund) => (
                  <th key={fund.schemeCode} className="p-3 text-left font-semibold">
                    {fund.schemeName}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {ROWS.map((row) => (
                <tr key={row.label} className="border-b border-border/60 last:border-0">
                  <td className="p-3 font-medium text-muted-foreground">{row.label}</td>
                  {comparison.funds.map((fund) => (
                    <td key={fund.schemeCode + row.key} className="p-3">
                      {formatCell(row.key, fund[row.key])}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Disclaimer text={comparison?.disclaimer} />
    </div>
  );
}
