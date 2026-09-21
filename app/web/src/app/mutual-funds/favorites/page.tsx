"use client";

import Link from "next/link";
import { Heart } from "lucide-react";
import { RequireAuth } from "@/components/auth/require-auth";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Pagination } from "@/components/mutual-funds/pagination";
import { useFavoriteMutualFunds, useToggleFavorite } from "@/hooks/use-mutual-funds";
import { ROUTES } from "@/lib/constants";
import { formatNav, formatPercent } from "@/lib/utils";
import { useState } from "react";

function FavoritesContent() {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError, refetch } = useFavoriteMutualFunds(page);
  const { remove } = useToggleFavorite();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Favorite Mutual Funds</h1>
        <p className="text-sm text-muted-foreground">Funds you have saved for quick access.</p>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-40 w-full" />
          ))}
        </div>
      ) : isError ? (
        <ErrorState title="Could not load your favorites" onRetry={() => refetch()} />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          icon={Heart}
          title="No favorites yet"
          description="Browse the mutual fund explorer and tap the heart icon to save funds here."
          action={
            <Button asChild size="sm">
              <Link href={ROUTES.mutualFunds}>Explore Mutual Funds</Link>
            </Button>
          }
        />
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {data.content.map(({ fund }) => (
              <Card key={fund.schemeCode}>
                <CardContent className="space-y-3 pt-6">
                  <div>
                    <p className="font-semibold">{fund.schemeName}</p>
                    <p className="text-xs text-muted-foreground">{fund.amcName}</p>
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    <Badge variant="outline">{fund.category}</Badge>
                    <Badge variant="secondary">{fund.riskLevel.replace(/_/g, " ")}</Badge>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    <div>
                      <p className="text-[11px] uppercase text-muted-foreground">NAV</p>
                      <p className="font-medium">{formatNav(fund.nav)}</p>
                    </div>
                    <div>
                      <p className="text-[11px] uppercase text-muted-foreground">1Y Return</p>
                      <p className="font-medium">{formatPercent(fund.oneYearReturn)}</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" className="flex-1" onClick={() => remove.mutate(fund.schemeCode)}>
                      Remove
                    </Button>
                    <Button size="sm" className="flex-1" asChild>
                      <Link href={ROUTES.mutualFundDetails(fund.schemeCode)}>Open Details</Link>
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}

export default function MutualFundFavoritesPage() {
  return (
    <RequireAuth>
      <FavoritesContent />
    </RequireAuth>
  );
}
