import type { Metadata } from "next";
import { DashboardNav } from "@/components/dashboard/dashboard-nav";

export const metadata: Metadata = {
  title: "Stocks | FinAdvisor",
  description:
    "Search stocks and view provider-backed quotes, OHLC history and news. Informational data only - not investment advice.",
};

export default function StocksLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-muted/20">
      <DashboardNav />
      <main className="container py-8">{children}</main>
    </div>
  );
}
