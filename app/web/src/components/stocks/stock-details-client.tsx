"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ErrorState } from "@/components/ui/error-state";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { StockQuotePanel } from "@/components/stocks/stock-quote-panel";
import { useStockDetails, useStockNews, useStockPrice } from "@/hooks/use-stocks";
import { ROUTES } from "@/lib/constants";
import { formatDate } from "@/lib/utils";

const STOCK_DISCLAIMER =
  "Stock market data may be delayed, subject to provider availability, data-source limitations and synchronization intervals. This platform provides informational data only and does not constitute investment advice.";

// Read via a query param (not a dynamic path segment) so this page works under static export (GitHub Pages).
export function StockDetailsClient() {
  const symbol = useSearchParams().get("symbol") ?? "";

  const { data: details, isLoading, isError, refetch } = useStockDetails(symbol);
  const { data: quote, isLoading: quoteLoading } = useStockPrice(symbol);
  const { data: news } = useStockNews(symbol);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-40 w-full" />
      </div>
    );
  }

  if (isError || !details) {
    return <ErrorState title="Could not load this stock" description="Please try again." onRetry={() => refetch()} />;
  }

  return (
    <div className="space-y-6 pb-20">
      <Link href={ROUTES.stocks} className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back to stocks
      </Link>

      <div>
        <h1 className="text-2xl font-bold">{details.symbol}</h1>
        <p className="text-sm text-muted-foreground">
          {details.companyName} · {details.exchange}
          {details.sector ? ` · ${details.sector}` : ""}
        </p>
      </div>

      {quoteLoading ? (
        <Skeleton className="h-32 w-full" />
      ) : quote ? (
        <StockQuotePanel quote={quote} />
      ) : (
        <p className="text-sm text-muted-foreground">Quote unavailable right now.</p>
      )}

      {news && news.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Recent News</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {news.map((item, idx) => (
              <div key={idx} className="border-b border-border/60 pb-3 last:border-0 last:pb-0">
                {item.articleLink ? (
                  <a href={item.articleLink} target="_blank" rel="noopener noreferrer" className="font-medium hover:underline">
                    {item.heading}
                  </a>
                ) : (
                  <p className="font-medium">{item.heading}</p>
                )}
                {item.summary && <p className="mt-1 text-sm text-muted-foreground">{item.summary}</p>}
                {item.publishedAt && <p className="mt-1 text-xs text-muted-foreground">{formatDate(item.publishedAt)}</p>}
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      <Disclaimer text={STOCK_DISCLAIMER} />
    </div>
  );
}
