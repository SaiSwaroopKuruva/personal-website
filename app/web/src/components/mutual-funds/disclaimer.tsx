import { ShieldAlert } from "lucide-react";
import { cn } from "@/lib/utils";

interface DisclaimerProps {
  text?: string;
  className?: string;
}

const DEFAULT_DISCLAIMER =
  "Mutual fund investments are subject to market risks. Past performance is not indicative of, and does not guarantee, future returns. The information shown is for educational and informational purposes only and should not be treated as personalized investment advice.";

export function Disclaimer({ text = DEFAULT_DISCLAIMER, className }: DisclaimerProps) {
  return (
    <div
      role="note"
      aria-label="Investment risk disclaimer"
      className={cn(
        "flex items-start gap-2 rounded-lg border border-amber-300/60 bg-amber-50 p-3 text-xs text-amber-900 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-200",
        className
      )}
    >
      <ShieldAlert className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
      <p>{text}</p>
    </div>
  );
}
