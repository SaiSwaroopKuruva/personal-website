"use client";

import { Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useStockWatchlist, useToggleWatchlist } from "@/hooks/use-stocks";
import { useAuthStore } from "@/store/auth-store";
import { cn } from "@/lib/utils";

/** Watchlist star toggle (Part 29/37) - hidden entirely for logged-out users rather than prompting to log in inline. */
export function WatchlistButton({ symbol }: { symbol: string }) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const { data: watchlist } = useStockWatchlist();
  const { add, remove } = useToggleWatchlist();

  if (!accessToken) {
    return null;
  }

  const isWatched = watchlist?.some((item) => item.symbol === symbol) ?? false;
  const isPending = add.isPending || remove.isPending;

  return (
    <Button
      variant={isWatched ? "secondary" : "outline"}
      size="sm"
      isLoading={isPending}
      onClick={() => (isWatched ? remove.mutate(symbol) : add.mutate(symbol))}
      aria-pressed={isWatched}
      aria-label={isWatched ? `Remove ${symbol} from watchlist` : `Add ${symbol} to watchlist`}
    >
      <Star className={cn("h-4 w-4", isWatched && "fill-current")} />
      {isWatched ? "Watchlisted" : "Watchlist"}
    </Button>
  );
}
