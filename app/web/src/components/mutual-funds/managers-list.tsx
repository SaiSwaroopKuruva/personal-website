import { Badge } from "@/components/ui/badge";
import { formatDate } from "@/lib/utils";
import type { FundManager } from "@/types/mutual-fund";

export function ManagersList({ managers }: { managers: FundManager[] }) {
  if (managers.length === 0) {
    return <p className="text-sm text-muted-foreground">Fund manager information is not available for this fund.</p>;
  }

  return (
    <ul className="space-y-3">
      {managers.map((manager) => (
        <li key={manager.name} className="rounded-lg border border-border p-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="font-medium">{manager.name}</p>
              <p className="text-xs text-muted-foreground">{manager.designation ?? "Fund Manager"}</p>
            </div>
            <Badge variant={manager.active ? "success" : "outline"}>{manager.active ? "Active" : "Inactive"}</Badge>
          </div>
          <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
            {manager.experienceYears != null && <span>{manager.experienceYears} yrs experience</span>}
            {manager.joiningDate && <span>Managing since {formatDate(manager.joiningDate)}</span>}
          </div>
          {manager.bio && <p className="mt-2 text-sm text-muted-foreground">{manager.bio}</p>}
        </li>
      ))}
    </ul>
  );
}
