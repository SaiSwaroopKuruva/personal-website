import { ProgressBar } from "@/components/ui/progress-bar";
import { EmptyState } from "@/components/ui/empty-state";
import { formatInr } from "@/lib/utils";
import type { AllocationSlice } from "@/types/portfolio";
import { PieChart } from "lucide-react";

const COLORS = ["bg-primary", "bg-emerald-500", "bg-amber-500", "bg-sky-500", "bg-violet-500", "bg-rose-500", "bg-slate-500"];

export function AllocationBreakdown({ title, slices }: { title: string; slices: AllocationSlice[] }) {
  if (slices.length === 0) {
    return <EmptyState icon={PieChart} title={`No ${title.toLowerCase()} to show`} description="Add holdings to see an allocation breakdown." />;
  }

  return (
    <div className="space-y-3">
      {slices.map((slice, index) => (
        <div key={slice.label} className="space-y-1">
          <div className="flex items-center justify-between text-sm">
            <span className="font-medium">{slice.label}</span>
            <span className="text-muted-foreground">
              {formatInr(slice.marketValue)} · {Number(slice.percentage).toFixed(1)}%
            </span>
          </div>
          <ProgressBar value={Number(slice.percentage)} indicatorClassName={COLORS[index % COLORS.length]} />
        </div>
      ))}
    </div>
  );
}
