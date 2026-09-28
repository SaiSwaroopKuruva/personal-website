import { ArrowDown, ArrowUp, Minus } from "lucide-react";
import { cn } from "@/lib/utils";
import { formatPercent } from "@/lib/utils";

interface ChangeIndicatorProps {
  /** Numeric change percentage (positive/negative/zero). Accepts string | number | null to match API DTOs directly. */
  changePercent: string | number | null | undefined;
  className?: string;
  showIcon?: boolean;
}

/** Semantic market up/down/neutral indicator (Part 1/41) - never relies on color alone, always shows a sign and an icon. */
export function ChangeIndicator({ changePercent, className, showIcon = true }: ChangeIndicatorProps) {
  const numeric = changePercent === null || changePercent === undefined || changePercent === "" ? null : Number(changePercent);
  const direction = numeric === null || Number.isNaN(numeric) ? "neutral" : numeric > 0 ? "up" : numeric < 0 ? "down" : "neutral";

  const colorClass =
    direction === "up" ? "text-positive" : direction === "down" ? "text-negative" : "text-market-neutral";
  const Icon = direction === "up" ? ArrowUp : direction === "down" ? ArrowDown : Minus;

  return (
    <span className={cn("inline-flex items-center gap-1 font-medium", colorClass, className)}>
      {showIcon && <Icon className="h-3.5 w-3.5" aria-hidden="true" />}
      {formatPercent(changePercent)}
    </span>
  );
}
