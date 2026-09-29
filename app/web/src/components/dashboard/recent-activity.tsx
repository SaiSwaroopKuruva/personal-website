"use client";

import { ArrowDownLeft, ArrowUpRight } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { useSelectedPortfolio, usePortfolioTransactions } from "@/hooks/use-portfolio";
import { formatDate, formatInr } from "@/lib/utils";

const CREDIT_TYPES = new Set(["SELL", "REDEMPTION", "DIVIDEND"]);

export function RecentActivity() {
  const { selectedPortfolioId, isLoading } = useSelectedPortfolio();
  const transactionsQuery = usePortfolioTransactions(selectedPortfolioId, { page: 0, size: 5 });

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Recent Activity</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {isLoading || transactionsQuery.isLoading ? (
          <Skeleton className="h-24 w-full" />
        ) : transactionsQuery.data && transactionsQuery.data.content.length > 0 ? (
          transactionsQuery.data.content.map((tx) => {
            const isCredit = CREDIT_TYPES.has(tx.transactionType);
            return (
              <div key={tx.id} className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div
                    className={`flex h-9 w-9 items-center justify-center rounded-full ${
                      isCredit
                        ? "bg-emerald-100 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400"
                        : "bg-red-100 text-red-600 dark:bg-red-500/10 dark:text-red-400"
                    }`}
                  >
                    {isCredit ? <ArrowDownLeft className="h-4 w-4" /> : <ArrowUpRight className="h-4 w-4" />}
                  </div>
                  <div>
                    <p className="text-sm font-medium">
                      {tx.transactionType} - {tx.assetType === "STOCK" ? tx.stockSymbol : tx.mutualFundSchemeCode}
                    </p>
                    <p className="text-xs text-muted-foreground">{formatDate(tx.transactionDate)}</p>
                  </div>
                </div>
                <p className="text-sm font-semibold">{formatInr(tx.netAmount)}</p>
              </div>
            );
          })
        ) : (
          <EmptyState title="No recent activity" description="Record a transaction to see it here." />
        )}
      </CardContent>
    </Card>
  );
}
