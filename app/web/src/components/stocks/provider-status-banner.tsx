"use client";

import { useProviderStatus } from "@/hooks/use-provider-status";
import { cn } from "@/lib/utils";

const STATUS_STYLES: Record<string, string> = {
  CONNECTED: "bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400",
  DEGRADED: "bg-amber-100 text-amber-800 dark:bg-amber-500/10 dark:text-amber-300",
  UNAVAILABLE: "bg-destructive/10 text-destructive",
  DISABLED: "bg-muted text-muted-foreground",
};

/** Surfaces provider connectivity honestly instead of silently showing stale/placeholder data (Part 27/30). */
export function ProviderStatusBanner() {
  const { data } = useProviderStatus();
  if (!data) return null;

  const entries = [
    { label: "Stocks (Upstox)", status: data.stocks },
    { label: "Mutual Funds (AMFI)", status: data.mutualFunds },
  ];

  return (
    <div className="flex flex-wrap gap-2 text-xs">
      {entries.map((entry) => (
        <span
          key={entry.label}
          className={cn("rounded-full px-2.5 py-1 font-medium", STATUS_STYLES[entry.status.status] ?? STATUS_STYLES.DISABLED)}
          title={entry.status.message ?? undefined}
        >
          {entry.label}: {entry.status.status}
        </span>
      ))}
    </div>
  );
}
