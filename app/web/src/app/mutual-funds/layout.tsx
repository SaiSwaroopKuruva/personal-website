import type { Metadata } from "next";
import { DashboardNav } from "@/components/dashboard/dashboard-nav";

export const metadata: Metadata = {
  title: "Mutual Funds | FinAdvisor",
  description:
    "Search, filter and compare mutual funds available to Indian investors. Educational information only - not investment advice.",
};

export default function MutualFundsLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-muted/20">
      <DashboardNav />
      <main className="container py-8">{children}</main>
    </div>
  );
}
