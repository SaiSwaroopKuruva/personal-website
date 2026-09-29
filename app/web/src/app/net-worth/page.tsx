"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ErrorState } from "@/components/ui/error-state";
import { NetWorthSummaryCards } from "@/components/net-worth/net-worth-summary-cards";
import { NetWorthHistoryChart } from "@/components/net-worth/net-worth-history-chart";
import { AssetLiabilityManager } from "@/components/net-worth/asset-liability-manager";
import { useNetWorthHistory, useNetWorthSummary } from "@/hooks/use-net-worth";

export default function NetWorthPage() {
  const summaryQuery = useNetWorthSummary();
  const historyQuery = useNetWorthHistory();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Net Worth</h1>
        <p className="text-sm text-muted-foreground">{summaryQuery.data?.disclosure ?? "Track your assets, liabilities and net worth."}</p>
      </div>

      {summaryQuery.isLoading ? (
        <Skeleton className="h-32 w-full" />
      ) : summaryQuery.isError || !summaryQuery.data ? (
        <ErrorState onRetry={() => summaryQuery.refetch()} />
      ) : (
        <NetWorthSummaryCards summary={summaryQuery.data} />
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Net Worth Trend</CardTitle>
        </CardHeader>
        <CardContent>
          {historyQuery.isLoading ? (
            <Skeleton className="h-48 w-full" />
          ) : (
            <>
              <NetWorthHistoryChart points={historyQuery.data?.points ?? []} />
              {historyQuery.data?.limitationMessage ? (
                <p className="mt-2 text-xs text-muted-foreground">{historyQuery.data.limitationMessage}</p>
              ) : null}
            </>
          )}
        </CardContent>
      </Card>

      <AssetLiabilityManager />
    </div>
  );
}
