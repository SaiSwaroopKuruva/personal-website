"use client";

import { useState } from "react";
import { Filter, Search as SearchIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { FundCard } from "@/components/mutual-funds/fund-card";
import { FundFilters, type FundFilterValues } from "@/components/mutual-funds/fund-filters";
import { CompareBar } from "@/components/mutual-funds/compare-bar";
import { Pagination } from "@/components/mutual-funds/pagination";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { useMutualFundSearch } from "@/hooks/use-mutual-funds";
import { useDebouncedValue } from "@/hooks/use-debounced-value";

const PAGE_SIZE = 12;
const SORT_OPTIONS = [
  { label: "Name (A-Z)", value: "schemeName-ASC" },
  { label: "NAV (High to Low)", value: "nav-DESC" },
  { label: "AUM (High to Low)", value: "aum-DESC" },
  { label: "Expense Ratio (Low to High)", value: "expenseRatio-ASC" },
];

export default function MutualFundsExplorerPage() {
  const [searchInput, setSearchInput] = useState("");
  const search = useDebouncedValue(searchInput, 400);
  const [filters, setFilters] = useState<FundFilterValues>({});
  const [sortValue, setSortValue] = useState(SORT_OPTIONS[0].value);
  const [page, setPage] = useState(0);
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);

  const [sort, direction] = sortValue.split("-") as [string, "ASC" | "DESC"];

  const { data, isLoading, isError, isFetching, refetch } = useMutualFundSearch({
    search: search || undefined,
    ...filters,
    sort,
    direction,
    page,
    size: PAGE_SIZE,
  });

  const handleFilterChange = (values: FundFilterValues) => {
    setFilters(values);
    setPage(0);
  };

  return (
    <div className="space-y-6 pb-20">
      <div>
        <h1 className="text-2xl font-bold">Mutual Fund Explorer</h1>
        <p className="text-sm text-muted-foreground">
          Search, filter and compare mutual funds available to Indian investors.
        </p>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <SearchIcon className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={searchInput}
            onChange={(e) => {
              setSearchInput(e.target.value);
              setPage(0);
            }}
            placeholder="Search by fund name or scheme code..."
            className="pl-9"
            aria-label="Search mutual funds"
          />
        </div>
        <select
          aria-label="Sort funds"
          value={sortValue}
          onChange={(e) => setSortValue(e.target.value)}
          className="h-10 rounded-lg border border-input bg-background px-3 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring sm:w-64"
        >
          {SORT_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <Button variant="outline" className="sm:hidden" onClick={() => setMobileFiltersOpen(true)}>
          <Filter className="h-4 w-4" /> Filters
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[240px_1fr]">
        <aside className="hidden lg:block">
          <FundFilters values={filters} onChange={handleFilterChange} onReset={() => handleFilterChange({})} />
        </aside>

        <Dialog open={mobileFiltersOpen} onOpenChange={setMobileFiltersOpen}>
          <DialogContent className="max-h-[85vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Filters</DialogTitle>
            </DialogHeader>
            <FundFilters values={filters} onChange={handleFilterChange} onReset={() => handleFilterChange({})} />
          </DialogContent>
        </Dialog>

        <div className="space-y-4">
          {isLoading ? (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
              {Array.from({ length: 6 }).map((_, i) => (
                <Skeleton key={i} className="h-56 w-full" />
              ))}
            </div>
          ) : isError ? (
            <ErrorState
              title="Could not load mutual funds"
              description="Please check your connection and try again."
              onRetry={() => refetch()}
            />
          ) : !data || data.content.length === 0 ? (
            <EmptyState
              icon={SearchIcon}
              title="No mutual funds found"
              description="Try adjusting your search or filters."
            />
          ) : (
            <>
              <p className="text-sm text-muted-foreground" aria-live="polite">
                {data.totalElements} fund{data.totalElements === 1 ? "" : "s"} found
                {isFetching ? " · updating…" : ""}
              </p>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
                {data.content.map((fund) => (
                  <FundCard key={fund.schemeCode} fund={fund} />
                ))}
              </div>
              <Pagination page={data.page} totalPages={data.totalPages} onPageChange={setPage} />
            </>
          )}

          <Disclaimer />
        </div>
      </div>

      <CompareBar />
    </div>
  );
}
