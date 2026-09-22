import type { Metadata } from "next";
import { DashboardNav } from "@/components/dashboard/dashboard-nav";

export const metadata: Metadata = {
  title: "Market Overview | FinAdvisor",
  description: "Market indices, gainers, losers and exchange status. Informational data only - not investment advice.",
};

export default function MarketLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-muted/20">
      <DashboardNav />
      <main className="container py-8">{children}</main>
    </div>
  );
}
