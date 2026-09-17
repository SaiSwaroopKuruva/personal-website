import { Clock } from "lucide-react";
import type { LoginHistoryEntry } from "@/types/security";

interface SecurityTimelineProps {
  entries: LoginHistoryEntry[];
}

const ACTION_LABELS: Record<string, string> = {
  LOGIN: "Signed in",
  REGISTER: "Account created",
  LOGOUT_ALL_DEVICES: "Logged out of all devices",
  DEVICE_REVOKED: "Revoked a device",
};

export function SecurityTimeline({ entries }: SecurityTimelineProps) {
  return (
    <ol className="space-y-4 border-l border-border pl-4">
      {entries.map((entry) => (
        <li key={entry.id} className="relative">
          <span className="absolute -left-[21px] top-1 h-2.5 w-2.5 rounded-full bg-primary" />
          <p className="text-sm font-medium">{ACTION_LABELS[entry.action] ?? entry.action}</p>
          <p className="flex items-center gap-1 text-xs text-muted-foreground">
            <Clock className="h-3 w-3" /> {new Date(entry.createdAt).toLocaleString()}
            {entry.ipAddress ? ` · ${entry.ipAddress}` : ""}
          </p>
        </li>
      ))}
    </ol>
  );
}
