"use client";

import Link from "next/link";
import { PiggyBank } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useNetWorthSummary } from "@/hooks/use-net-worth";
import { ROUTES } from "@/lib/constants";
import { formatInr } from "@/lib/utils";

export function NetWorthCard() {
  const summaryQuery = useNetWorthSummary();

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">Net Worth</CardTitle>
        <PiggyBank className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        {summaryQuery.isLoading ? (
          <Skeleton className="h-10 w-full" />
        ) : summaryQuery.data ? (
          <Link href={ROUTES.netWorth}>
            <p className="text-2xl font-bold">{formatInr(summaryQuery.data.netWorth)}</p>
            <p className="mt-1 text-xs text-muted-foreground">
              Assets {formatInr(summaryQuery.data.totalAssets)} · Liabilities {formatInr(summaryQuery.data.totalLiabilities)}
            </p>
          </Link>
        ) : (
          <div>
            <p className="text-sm text-muted-foreground">No data yet</p>
            <Link href={ROUTES.netWorth} className="text-xs text-primary hover:underline">
              Add assets and liabilities
            </Link>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

