export type AssetCategory = "CASH_AND_SAVINGS" | "FIXED_DEPOSIT" | "OTHER_INVESTMENT" | "PROPERTY" | "GOLD" | "OTHER";

export type LiabilityCategory = "HOME_LOAN" | "PERSONAL_LOAN" | "VEHICLE_LOAN" | "CREDIT_CARD" | "OTHER";

export interface UserAsset {
  id: string;
  name: string;
  category: AssetCategory;
  currentValue: string | number;
  currency: string;
  valuationDate: string;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UserLiability {
  id: string;
  name: string;
  category: LiabilityCategory;
  currentValue: string | number;
  currency: string;
  valuationDate: string;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AssetInput {
  name: string;
  category: AssetCategory;
  currentValue: number;
  currency?: string;
  valuationDate: string;
  notes?: string | null;
}

export interface LiabilityInput {
  name: string;
  category: LiabilityCategory;
  currentValue: number;
  currency?: string;
  valuationDate: string;
  notes?: string | null;
}

export interface NetWorthSummary {
  totalAssets: string | number;
  totalLiabilities: string | number;
  netWorth: string | number;
  investmentAssets: string | number;
  nonInvestmentAssets: string | number;
  partialInvestmentValuation: boolean;
  asOf: string;
  disclosure: string;
}

export interface NetWorthHistoryPoint {
  date: string;
  investmentValue: string | number;
  nonInvestmentAssetValue: string | number;
  liabilities: string | number;
  netWorth: string | number;
}

export interface NetWorthHistory {
  points: NetWorthHistoryPoint[];
  limitationMessage: string | null;
}
