import { formatInr } from "@/lib/utils";

interface AmountBarChartProps {
  segments: { label: string; value: number; className: string }[];
}

/** Simple proportional stacked bar (no chart library) showing invested vs. estimated returns. */
export function AmountBarChart({ segments }: AmountBarChartProps) {
  const total = segments.reduce((sum, s) => sum + Math.max(s.value, 0), 0) || 1;

  return (
    <div className="space-y-2">
      <div className="flex h-4 w-full overflow-hidden rounded-full bg-muted">
        {segments.map((segment) => (
          <div
            key={segment.label}
            className={segment.className}
            style={{ width: `${(Math.max(segment.value, 0) / total) * 100}%` }}
            title={`${segment.label}: ${formatInr(segment.value)}`}
          />
        ))}
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs">
        {segments.map((segment) => (
          <span key={segment.label} className="flex items-center gap-1.5">
            <span className={`h-2.5 w-2.5 rounded-full ${segment.className}`} />
            {segment.label}: <span className="font-medium">{formatInr(segment.value)}</span>
          </span>
        ))}
      </div>
    </div>
  );
}
