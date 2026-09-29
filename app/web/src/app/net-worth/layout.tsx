import type { Metadata } from "next";
import { RequireAuth } from "@/components/auth/require-auth";
import { DashboardNav } from "@/components/dashboard/dashboard-nav";

export const metadata: Metadata = {
  title: "Net Worth | FinAdvisor",
  description: "Track your assets, liabilities and net worth alongside your investment portfolios.",
};

export default function NetWorthLayout({ children }: { children: React.ReactNode }) {
  return (
    <RequireAuth>
      <div className="min-h-screen bg-muted/20">
        <DashboardNav />
        <main className="container space-y-6 py-8">{children}</main>
      </div>
    </RequireAuth>
  );
}
