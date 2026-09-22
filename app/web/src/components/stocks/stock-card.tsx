import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { ROUTES } from "@/lib/constants";
import type { StockDetails } from "@/types/stock";

export function StockCard({ stock }: { stock: StockDetails }) {
  return (
    <Link
      href={ROUTES.stockDetails(stock.symbol)}
      className="flex items-center justify-between rounded-lg border border-border/60 bg-card p-4 transition-colors hover:border-primary/40 hover:bg-muted/40"
    >
      <div className="min-w-0">
        <p className="truncate font-semibold">{stock.symbol}</p>
        <p className="truncate text-sm text-muted-foreground">{stock.companyName}</p>
        <p className="text-xs text-muted-foreground">{stock.exchange}</p>
      </div>
      <ArrowRight className="h-4 w-4 shrink-0 text-muted-foreground" aria-hidden="true" />
    </Link>
  );
}
