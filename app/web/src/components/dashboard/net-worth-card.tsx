import { PiggyBank } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

// Placeholder data — real net worth aggregation is planned for a later phase.
const NET_WORTH = { total: "₹28,90,500", change: "+3.2% this month" };

export function NetWorthCard() {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">Net Worth</CardTitle>
        <PiggyBank className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        <p className="text-2xl font-bold">{NET_WORTH.total}</p>
        <p className="mt-1 text-xs text-muted-foreground">{NET_WORTH.change}</p>
      </CardContent>
    </Card>
  );
}
