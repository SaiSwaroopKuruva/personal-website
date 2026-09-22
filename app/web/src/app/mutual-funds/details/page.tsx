import type { Metadata } from "next";
import { Suspense } from "react";
import { MutualFundDetailsClient } from "@/components/mutual-funds/mutual-fund-details-client";

// Static export (GitHub Pages) can't pre-render arbitrary scheme codes as dynamic path segments, so this
// route reads ?schemeCode= as a query param and fetches/labels the fund entirely client-side instead.
export const metadata: Metadata = {
  title: "Mutual Fund Details | FinAdvisor",
  description:
    "View NAV, NAV history, returns, holdings and fund manager details for a mutual fund scheme. Educational information only, not investment advice.",
};

export default function MutualFundDetailsPage() {
  return (
    <Suspense fallback={<div className="py-12 text-center text-sm text-muted-foreground">Loading fund details…</div>}>
      <MutualFundDetailsClient />
    </Suspense>
  );
}
