"use client";

import { useState } from "react";
import Link from "next/link";
import { toast } from "sonner";
import { ArrowLeft, Heart, Scale, Share2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ErrorState } from "@/components/ui/error-state";
import { NavChart } from "@/components/mutual-funds/nav-chart";
import { ReturnsTable } from "@/components/mutual-funds/returns-table";
import { HoldingsTable } from "@/components/mutual-funds/holdings-table";
import { ManagersList } from "@/components/mutual-funds/managers-list";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { useMutualFundDetails, useMutualFundHoldings, useMutualFundNavHistory, useToggleFavorite } from "@/hooks/use-mutual-funds";
import { useAuthStore } from "@/store/auth-store";
import { useCompareStore } from "@/store/compare-store";
import { ROUTES } from "@/lib/constants";
import { cn, formatCrores, formatDate, formatNav } from "@/lib/utils";

const NAV_RANGES = ["1M", "3M", "6M", "1Y", "3Y", "5Y", "MAX"];

export function MutualFundDetailsClient({ schemeCode }: { schemeCode: string }) {
  const [range, setRange] = useState("1Y");

  const { data: fund, isLoading, isError, refetch } = useMutualFundDetails(schemeCode);
  const { data: navHistory } = useMutualFundNavHistory(schemeCode, range);
  const { data: holdingsPage } = useMutualFundHoldings(schemeCode);

  const accessToken = useAuthStore((state) => state.accessToken);
  const { add: addFavorite, remove: removeFavorite } = useToggleFavorite();
  const { add: addCompare, remove: removeCompare, schemeCodes, isFull } = useCompareStore();

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-40 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (isError || !fund) {
    return <ErrorState title="Could not load this fund" description="Please try again." onRetry={() => refetch()} />;
  }

  const inCompare = schemeCodes.includes(fund.schemeCode);

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

  const handleCompareToggle = () => {
    if (inCompare) {
      removeCompare(fund.schemeCode);
      return;
    }
    if (isFull()) {
      toast.error("You can compare up to 4 mutual funds at a time");
      return;
    }
    addCompare(fund.schemeCode);
  };

  const handleShare = async () => {
    const url = typeof window !== "undefined" ? window.location.href : "";
    if (navigator.share) {
      await navigator.share({ title: fund.schemeName, url }).catch(() => undefined);
    } else if (navigator.clipboard) {
      await navigator.clipboard.writeText(url);
      toast.success("Link copied to clipboard");
    }
  };

  return (
    <div className="space-y-6 pb-10">
      <Link href={ROUTES.mutualFunds} className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back to explorer
      </Link>

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">{fund.schemeName}</h1>
          <p className="text-sm text-muted-foreground">{fund.amcName}</p>
          <div className="mt-2 flex flex-wrap items-center gap-1.5">
            <Badge variant="outline">{fund.category}</Badge>
            {fund.subCategory && <Badge variant="outline">{fund.subCategory}</Badge>}
            <Badge variant="secondary">{fund.riskLevel.replace(/_/g, " ")}</Badge>
            <Badge variant="outline">{fund.planType} · {fund.optionType}</Badge>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={handleFavoriteToggle} aria-pressed={fund.favorite}>
            <Heart className={cn("h-4 w-4", fund.favorite && "fill-destructive text-destructive")} />
            {fund.favorite ? "Favorited" : "Favorite"}
          </Button>
          <Button variant="outline" size="sm" onClick={handleCompareToggle}>
            <Scale className="h-4 w-4" /> {inCompare ? "Remove" : "Compare"}
          </Button>
          <Button variant="outline" size="sm" onClick={handleShare} aria-label="Share this fund">
            <Share2 className="h-4 w-4" />
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <SummaryStat label="NAV" value={formatNav(fund.nav)} sub={fund.navDate ? formatDate(fund.navDate) : undefined} />
        <SummaryStat label="AUM" value={formatCrores(fund.aum)} />
        <SummaryStat label="Expense Ratio" value={fund.expenseRatio ? `${fund.expenseRatio}%` : "—"} />
        <SummaryStat label="Min. SIP / Lumpsum" value={`${formatCrores0(fund.minimumSip)} / ${formatCrores0(fund.minimumLumpsum)}`} />
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-wrap items-center justify-between gap-2">
            <CardTitle>NAV Performance</CardTitle>
            <div className="flex flex-wrap gap-1">
              {NAV_RANGES.map((r) => (
                <button
                  key={r}
                  onClick={() => setRange(r)}
                  aria-pressed={range === r}
                  className={`rounded-md px-2.5 py-1 text-xs font-medium transition-colors ${
                    range === r ? "bg-primary text-primary-foreground" : "bg-muted hover:bg-accent"
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <NavChart points={navHistory?.points ?? []} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Returns</CardTitle>
        </CardHeader>
        <CardContent>
          <ReturnsTable returns={fund.returns} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Portfolio Holdings</CardTitle>
        </CardHeader>
        <CardContent>
          <HoldingsTable holdings={holdingsPage?.content ?? fund.topHoldings} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Fund Managers</CardTitle>
        </CardHeader>
        <CardContent>
          <ManagersList managers={fund.managers} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Fund Information</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 gap-x-6 gap-y-3 text-sm sm:grid-cols-2">
          <InfoRow label="Investment Objective" value={fund.investmentObjective ?? "—"} />
          <InfoRow label="Benchmark" value={fund.benchmark ?? "—"} />
          <InfoRow label="Exit Load" value={fund.exitLoad ?? "—"} />
          <InfoRow label="Inception Date" value={fund.inceptionDate ? formatDate(fund.inceptionDate) : "—"} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Plan an Investment</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-2">
          <Button asChild>
            <Link href={ROUTES.calculatorSip}>SIP Calculator</Link>
          </Button>
          <Button variant="outline" asChild>
            <Link href={ROUTES.calculatorLumpsum}>Lumpsum Calculator</Link>
          </Button>
          <Button variant="outline" asChild>
            <Link href={`${ROUTES.mutualFunds}?category=${encodeURIComponent(fund.category)}`}>
              Explore similar {fund.category} funds
            </Link>
          </Button>
        </CardContent>
      </Card>

      <Disclaimer text={fund.disclaimer} />
    </div>
  );
}

function SummaryStat({ label, value, sub }: { label: string; value: string; sub?: string }) {
  return (
    <div className="rounded-lg border border-border bg-card p-3">
      <p className="text-[11px] uppercase tracking-wide text-muted-foreground">{label}</p>
      <p className="text-lg font-semibold">{value}</p>
      {sub && <p className="text-xs text-muted-foreground">{sub}</p>}
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-muted-foreground">{label}</p>
      <p>{value}</p>
    </div>
  );
}

function formatCrores0(value: string | null) {
  if (!value) return "—";
  const numeric = Number(value);
  if (Number.isNaN(numeric)) return "—";
  return `₹${new Intl.NumberFormat("en-IN").format(numeric)}`;
}
