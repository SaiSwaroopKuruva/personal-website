import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

// Placeholder data — real market data integration is planned for a later phase.
const MARKET_INDICES = [
  { name: "NIFTY 50", value: "24,812.30", change: "+0.42%", positive: true },
  { name: "SENSEX", value: "81,559.20", change: "+0.38%", positive: true },
  { name: "NIFTY BANK", value: "51,203.10", change: "-0.15%", positive: false },
];

export function MarketSnapshot() {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Market Snapshot</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {MARKET_INDICES.map((index) => (
          <div key={index.name} className="flex items-center justify-between text-sm">
            <span className="font-medium">{index.name}</span>
            <div className="flex items-center gap-2">
              <span>{index.value}</span>
              <Badge variant={index.positive ? "success" : "destructive"}>{index.change}</Badge>
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
