import { WelcomeCard } from "@/components/dashboard/welcome-card";
import { PortfolioSummaryCard } from "@/components/dashboard/portfolio-summary-card";
import { NetWorthCard } from "@/components/dashboard/net-worth-card";
import { GainLossCard } from "@/components/dashboard/gain-loss-card";
import { QuickActions } from "@/components/dashboard/quick-actions";
import { RecentActivity } from "@/components/dashboard/recent-activity";
import { InvestmentGoals } from "@/components/dashboard/investment-goals";
import { MarketSnapshot } from "@/components/dashboard/market-snapshot";

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <WelcomeCard />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <PortfolioSummaryCard />
        <NetWorthCard />
        <GainLossCard />
      </div>

      <QuickActions />

      <div className="grid gap-4 lg:grid-cols-3">
        <RecentActivity />
        <InvestmentGoals />
        <MarketSnapshot />
      </div>
    </div>
  );
}
