"use client";

import Link from "next/link";
import { Heart, Scale } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { useToggleFavorite } from "@/hooks/use-mutual-funds";
import { useAuthStore } from "@/store/auth-store";
import { useCompareStore } from "@/store/compare-store";
import { ROUTES } from "@/lib/constants";
import { cn, formatNav, formatPercent } from "@/lib/utils";
import type { MutualFundSummary } from "@/types/mutual-fund";
import { toast } from "sonner";

const RISK_BADGE_VARIANT: Record<string, "success" | "secondary" | "destructive" | "outline"> = {
  LOW: "success",
  LOW_TO_MODERATE: "success",
  MODERATE: "secondary",
  MODERATELY_HIGH: "secondary",
  HIGH: "destructive",
  VERY_HIGH: "destructive",
};

export function FundCard({ fund }: { fund: MutualFundSummary }) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const { add, remove, isFull, schemeCodes } = useCompareStore();
  const { add: addFavorite, remove: removeFavorite } = useToggleFavorite();

  const inCompare = schemeCodes.includes(fund.schemeCode);

  const handleCompareToggle = () => {
    if (inCompare) {
      remove(fund.schemeCode);
      return;
    }
    if (isFull()) {
      toast.error("You can compare up to 4 mutual funds at a time");
      return;
    }
    add(fund.schemeCode);
  };

  const handleFavoriteToggle = () => {
    if (!accessToken) {
      toast.error("Log in to save funds to your favorites");
      return;
    }
    if (fund.favorite) {
      removeFavorite.mutate(fund.schemeCode);
    } else {
      addFavorite.mutate(fund.schemeCode);
    }
  };

  return (
    <Card className="flex h-full flex-col transition-shadow hover:shadow-md">
      <CardHeader className="flex-row items-start justify-between gap-2 space-y-0 pb-3">
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold" title={fund.schemeName}>
            {fund.schemeName}
          </p>
          <p className="text-xs text-muted-foreground">{fund.amcName}</p>
        </div>
        <Button
          variant="ghost"
          size="icon"
          aria-label={fund.favorite ? "Remove from favorites" : "Add to favorites"}
          aria-pressed={fund.favorite}
          onClick={handleFavoriteToggle}
          className="h-8 w-8 shrink-0"
        >
          <Heart className={cn("h-4 w-4", fund.favorite && "fill-destructive text-destructive")} />
        </Button>
      </CardHeader>
      <CardContent className="flex flex-1 flex-col gap-3 pt-0">
        <div className="flex flex-wrap items-center gap-1.5">
          <Badge variant="outline">{fund.category}</Badge>
          <Badge variant={RISK_BADGE_VARIANT[fund.riskLevel] ?? "outline"}>{fund.riskLevel.replace(/_/g, " ")}</Badge>
        </div>

        <div className="grid grid-cols-2 gap-2 text-sm">
          <Metric label="NAV" value={formatNav(fund.nav)} />
          <Metric label="Expense Ratio" value={fund.expenseRatio ? `${fund.expenseRatio}%` : "—"} />
          <Metric label="1Y Return" value={formatPercent(fund.oneYearReturn)} accent />
          <Metric label="3Y Return" value={formatPercent(fund.threeYearReturn)} accent />
        </div>

        <div className="mt-auto flex items-center gap-2 pt-2">
          <Button variant="outline" size="sm" className="flex-1" onClick={handleCompareToggle}>
            <Scale className="h-3.5 w-3.5" /> {inCompare ? "Remove" : "Compare"}
          </Button>
          <Button size="sm" className="flex-1" asChild>
            <Link href={ROUTES.mutualFundDetails(fund.schemeCode)}>View Details</Link>
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

function Metric({ label, value, accent }: { label: string; value: string; accent?: boolean }) {
  const isPositive = accent && value.startsWith("+");
  const isNegative = accent && value.startsWith("-");
  return (
    <div>
      <p className="text-[11px] uppercase tracking-wide text-muted-foreground">{label}</p>
      <p
        className={cn(
          "font-medium",
          isPositive && "text-emerald-600 dark:text-emerald-400",
          isNegative && "text-destructive"
        )}
      >
        {value}
      </p>
    </div>
  );
}
