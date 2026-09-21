export interface SipCalculatorInput {
  monthlyInvestment: number;
  expectedAnnualReturn: number;
  durationYears: number;
}

export interface SipCalculatorResult {
  monthlyInvestment: string;
  expectedAnnualReturn: string;
  durationYears: number;
  totalInvestment: string;
  estimatedReturns: string;
  futureValue: string;
  estimate: boolean;
  disclaimer: string;
}

export interface LumpsumCalculatorInput {
  principal: number;
  expectedAnnualReturn: number;
  durationYears: number;
}

export interface LumpsumCalculatorResult {
  principal: string;
  expectedAnnualReturn: string;
  durationYears: number;
  investedAmount: string;
  estimatedReturns: string;
  futureValue: string;
  estimate: boolean;
  disclaimer: string;
}

export interface SwpCalculatorInput {
  initialInvestment: number;
  withdrawalPerMonth: number;
  expectedAnnualReturn: number;
  durationYears: number;
}

export interface SwpCalculatorResult {
  initialInvestment: string;
  withdrawalPerMonth: string;
  expectedAnnualReturn: string;
  durationYears: number;
  totalWithdrawn: string;
  remainingValue: string;
  estimatedGrowth: string;
  exhausted: boolean;
  exhaustedAfterMonths: number | null;
  estimate: boolean;
  disclaimer: string;
}
