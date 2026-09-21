import type { Metadata } from "next";
import { DashboardNav } from "@/components/dashboard/dashboard-nav";

export const metadata: Metadata = {
  title: "Investment Calculators | FinAdvisor",
  description: "Estimate SIP, lumpsum and SWP outcomes. Educational estimates only - not a guarantee of returns.",
};

export default function CalculatorsLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-muted/20">
      <DashboardNav />
      <main className="container max-w-3xl py-8">{children}</main>
    </div>
  );
}
