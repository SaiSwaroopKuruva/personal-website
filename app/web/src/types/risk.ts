export type RiskLevel = "CONSERVATIVE" | "MODERATELY_CONSERVATIVE" | "BALANCED" | "GROWTH" | "AGGRESSIVE";

export interface RiskOption {
  code: string;
  label: string;
  score: number;
}

export interface RiskQuestion {
  code: string;
  text: string;
  helpText: string | null;
  options: RiskOption[];
}

export interface RiskAnswer {
  questionCode: string;
  optionCode: string;
}

export interface RiskRecommendation {
  summary: string;
  suggestedAllocation: Record<string, number>;
  suggestedInvestmentHorizon: string;
  suggestedFundCategories: string[];
}

export interface RiskAssessmentResponse {
  id: string;
  score: number;
  riskLevel: RiskLevel;
  recommendation: RiskRecommendation;
  createdAt: string;
}
