import { TrendingUp } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

// Placeholder data — real market-linked gain/loss calculation is planned for a later phase.
const GAIN_LOSS = { amount: "+₹8,420", percent: "+0.68%" };

export function GainLossCard() {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">Today&apos;s Gain/Loss</CardTitle>
        <TrendingUp className="h-4 w-4 text-emerald-500" />
      </CardHeader>
      <CardContent>
        <p className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">{GAIN_LOSS.amount}</p>
        <p className="mt-1 text-xs text-muted-foreground">{GAIN_LOSS.percent} today</p>
      </CardContent>
    </Card>
  );
}
