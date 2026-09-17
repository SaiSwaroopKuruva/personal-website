import { ProgressBar } from "@/components/ui/progress-bar";

interface AllocationChartProps {
  allocation: Record<string, number>;
}

const COLORS = [
  "bg-primary",
  "bg-emerald-500",
  "bg-amber-500",
  "bg-sky-500",
  "bg-violet-500",
  "bg-rose-500",
  "bg-slate-500",
];

export function AllocationChart({ allocation }: AllocationChartProps) {
  const entries = Object.entries(allocation).filter(([, value]) => value > 0);

  return (
    <div className="space-y-3">
      {entries.map(([category, value], index) => (
        <div key={category} className="space-y-1">
          <div className="flex items-center justify-between text-sm">
            <span className="font-medium">{category}</span>
            <span className="text-muted-foreground">{value}%</span>
          </div>
          <ProgressBar value={value} indicatorClassName={COLORS[index % COLORS.length]} />
        </div>
      ))}
    </div>
  );
}
