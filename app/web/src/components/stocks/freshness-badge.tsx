import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { DataFreshness } from "@/types/stock";

const LABELS: Record<DataFreshness, string> = {
  LIVE: "LIVE",
  FRESH: "FRESH",
  STALE: "STALE",
  UNKNOWN: "UNKNOWN",
};

const STYLES: Record<DataFreshness, string> = {
  LIVE: "border-transparent bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400",
  FRESH: "border-transparent bg-sky-100 text-sky-700 dark:bg-sky-500/10 dark:text-sky-400",
  STALE: "border-transparent bg-amber-100 text-amber-800 dark:bg-amber-500/10 dark:text-amber-300",
  UNKNOWN: "border-transparent bg-muted text-muted-foreground",
};

interface FreshnessBadgeProps {
  freshness: DataFreshness;
  className?: string;
}

/** Never labels data as LIVE unless the backend actually reported it as such (Part 20 - no fabricated freshness). */
export function FreshnessBadge({ freshness, className }: FreshnessBadgeProps) {
  return (
    <Badge className={cn(STYLES[freshness], className)} title={`Data freshness: ${LABELS[freshness]}`}>
      {freshness === "LIVE" && <span className="mr-1 inline-block h-1.5 w-1.5 animate-pulse rounded-full bg-emerald-500" aria-hidden="true" />}
      {LABELS[freshness]}
    </Badge>
  );
}
