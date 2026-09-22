import type { Metadata } from "next";
import { Suspense } from "react";
import { StockDetailsClient } from "@/components/stocks/stock-details-client";

// Static export (GitHub Pages) can't pre-render arbitrary symbols as dynamic path segments, so this route
// reads ?symbol= as a query param and fetches/renders the stock entirely client-side (mirrors mutual-funds/details).
export const metadata: Metadata = {
  title: "Stock Details | FinAdvisor",
  description: "View quote, OHLC history and recent news for a stock. Informational data only, not investment advice.",
};

export default function StockDetailsPage() {
  return (
    <Suspense fallback={<div className="py-12 text-center text-sm text-muted-foreground">Loading stock details…</div>}>
      <StockDetailsClient />
    </Suspense>
  );
}
