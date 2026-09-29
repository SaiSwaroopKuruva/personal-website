"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { RequireAuth } from "@/components/auth/require-auth";
import { DashboardNav } from "@/components/dashboard/dashboard-nav";
import { PortfolioSelector } from "@/components/portfolio/portfolio-selector";
import { useSelectedPortfolio } from "@/hooks/use-portfolio";
import { ROUTES } from "@/lib/constants";
import { cn } from "@/lib/utils";

const TABS = [
  { label: "Overview", href: ROUTES.portfolio },
  { label: "Holdings", href: ROUTES.portfolioHoldings },
  { label: "Transactions", href: ROUTES.portfolioTransactions },
  { label: "Performance", href: ROUTES.portfolioPerformance },
  { label: "Allocation", href: ROUTES.portfolioAllocation },
];

export default function PortfolioLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { portfolios, selectedPortfolioId, setSelectedPortfolioId } = useSelectedPortfolio();

  return (
    <RequireAuth>
      <div className="min-h-screen bg-muted/20">
        <DashboardNav />
        <main className="container space-y-6 py-8">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold">Portfolio</h1>
              <p className="text-sm text-muted-foreground">Track your investments, transactions and returns.</p>
            </div>
            <PortfolioSelector portfolios={portfolios} selectedPortfolioId={selectedPortfolioId} onSelect={setSelectedPortfolioId} />
          </div>

          <nav className="flex gap-1 overflow-x-auto border-b border-border">
            {TABS.map((tab) => (
              <Link
                key={tab.href}
                href={tab.href}
                className={cn(
                  "whitespace-nowrap border-b-2 px-3 py-2 text-sm font-medium transition-colors",
                  pathname === tab.href
                    ? "border-primary text-foreground"
                    : "border-transparent text-muted-foreground hover:text-foreground"
                )}
              >
                {tab.label}
              </Link>
            ))}
          </nav>

          {children}
        </main>
      </div>
    </RequireAuth>
  );
}
