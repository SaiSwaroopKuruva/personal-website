import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { ValuationStatus } from "@/types/portfolio";

const LABELS: Record<ValuationStatus, string> = {
  CURRENT: "Current",
  STALE: "Stale",
  UNAVAILABLE: "Unavailable",
};

/** Never lets a stale/unavailable valuation read as "live" (Part 10) - always shows the honest status. */
export function FreshnessBadge({ status, className }: { status: ValuationStatus; className?: string }) {
  const variant = status === "CURRENT" ? "success" : status === "STALE" ? "secondary" : "destructive";
  return (
    <Badge variant={variant} className={cn(className)}>
      {LABELS[status]}
    </Badge>
  );
}
