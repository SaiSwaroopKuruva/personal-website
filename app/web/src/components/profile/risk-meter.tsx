import { Badge } from "@/components/ui/badge";
import { CircularProgress } from "@/components/ui/circular-progress";
import type { RiskLevel } from "@/types/risk";

const RISK_LEVEL_LABELS: Record<RiskLevel, string> = {
  CONSERVATIVE: "Conservative",
  MODERATELY_CONSERVATIVE: "Moderately Conservative",
  BALANCED: "Balanced",
  GROWTH: "Growth",
  AGGRESSIVE: "Aggressive",
};

const RISK_LEVEL_BADGE_VARIANT: Record<RiskLevel, "secondary" | "default" | "success" | "destructive"> = {
  CONSERVATIVE: "secondary",
  MODERATELY_CONSERVATIVE: "secondary",
  BALANCED: "default",
  GROWTH: "success",
  AGGRESSIVE: "destructive",
};

interface RiskMeterProps {
  score: number;
  riskLevel: RiskLevel;
}

export function RiskMeter({ score, riskLevel }: RiskMeterProps) {
  return (
    <div className="flex flex-col items-center gap-3">
      <CircularProgress
        value={score}
        size={140}
        strokeWidth={12}
        label={
          <div className="flex flex-col items-center">
            <span className="text-3xl font-semibold">{score}</span>
            <span className="text-xs text-muted-foreground">/ 100</span>
          </div>
        }
      />
      <Badge variant={RISK_LEVEL_BADGE_VARIANT[riskLevel]}>{RISK_LEVEL_LABELS[riskLevel]}</Badge>
    </div>
  );
}
