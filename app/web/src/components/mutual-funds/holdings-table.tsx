import { formatDate, formatInr } from "@/lib/utils";
import type { FundHolding } from "@/types/mutual-fund";

export function HoldingsTable({ holdings }: { holdings: FundHolding[] }) {
  if (holdings.length === 0) {
    return <p className="text-sm text-muted-foreground">Portfolio holdings are not yet available for this fund.</p>;
  }

  const asOfDate = holdings[0]?.asOfDate;

  return (
    <div className="space-y-2">
      {asOfDate && <p className="text-xs text-muted-foreground">Holdings as of {formatDate(asOfDate)}</p>}
      <div className="overflow-x-auto">
        <table className="w-full min-w-[560px] text-sm">
          <thead>
            <tr className="border-b border-border text-left text-xs uppercase text-muted-foreground">
              <th className="py-2 pr-4 font-medium">Security</th>
              <th className="py-2 pr-4 font-medium">Sector</th>
              <th className="py-2 pr-4 font-medium">Type</th>
              <th className="py-2 pr-4 font-medium">Weight</th>
              <th className="py-2 font-medium">Market Value</th>
            </tr>
          </thead>
          <tbody>
            {holdings.map((holding) => (
              <tr key={`${holding.securityName}-${holding.isin ?? holding.sector}`} className="border-b border-border/60 last:border-0">
                <td className="py-2 pr-4 font-medium">{holding.securityName}</td>
                <td className="py-2 pr-4 text-muted-foreground">{holding.sector ?? "—"}</td>
                <td className="py-2 pr-4 text-muted-foreground">{holding.assetType}</td>
                <td className="py-2 pr-4">{holding.weightPercentage}%</td>
                <td className="py-2">{formatInr(holding.marketValue)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
