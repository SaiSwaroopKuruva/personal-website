export type AssetType = "STOCK" | "MUTUAL_FUND";

export type TransactionType = "BUY" | "SELL" | "PURCHASE" | "REDEMPTION" | "DIVIDEND" | "FEE" | "TAX" | "ADJUSTMENT";

export type ValuationStatus = "CURRENT" | "STALE" | "UNAVAILABLE";

export type XirrStatus = "CALCULATED" | "INSUFFICIENT_DATA" | "NO_VALID_SOLUTION" | "NON_CONVERGENT";

export interface Portfolio {
  id: string;
  name: string;
  description: string | null;
  baseCurrency: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
  archivedAt: string | null;
}

export interface PortfolioInput {
  name: string;
  description?: string | null;
  isDefault?: boolean;
}

export interface ValuationInfo {
  price: string | number | null;
  source: string | null;
  sourceTimestamp: string | null;
  retrievedAt: string;
  status: ValuationStatus;
}

export interface Holding {
  assetType: AssetType;
  symbol: string;
  exchange: string | null;
  name: string;
  quantity: string | number;
  averageCost: string | number;
  costBasis: string | number;
  valuation: ValuationInfo | null;
  marketValue: string | number | null;
  unrealizedGain: string | number | null;
  unrealizedGainPercent: string | number | null;
  allocationPercent: string | number | null;
}

export interface PortfolioHoldings {
  holdings: Holding[];
  totalCostBasis: string | number;
  totalMarketValue: string | number;
  partialValuation: boolean;
  partialValuationMessage: string | null;
  asOf: string;
}

export interface XirrResult {
  xirrPercent: string | number | null;
  status: XirrStatus;
  message: string | null;
  calculatedAt: string;
}

export interface PortfolioSummary {
  portfolioId: string;
  portfolioName: string;
  investedAmount: string | number;
  currentMarketValue: string | number | null;
  absoluteGainLoss: string | number;
  percentGainLoss: string | number | null;
  realizedGainLoss: string | number;
  unrealizedGainLoss: string | number;
  holdingsCount: number;
  stockAllocationPercent: string | number | null;
  mutualFundAllocationPercent: string | number | null;
  lastValuationAt: string | null;
  partialValuation: boolean;
  partialValuationMessage: string | null;
  xirr: XirrResult | null;
}

export interface AllocationSlice {
  label: string;
  marketValue: string | number;
  percentage: string | number;
  holdingsCount: number;
}

export interface Allocation {
  byAssetClass: AllocationSlice[];
  byHolding: AllocationSlice[];
  totalMarketValue: string | number;
  partialValuation: boolean;
  partialValuationMessage: string | null;
}

export type PerformanceRange = "1M" | "3M" | "6M" | "1Y" | "3Y" | "5Y" | "ALL";

export interface PerformancePoint {
  date: string;
  investedValue: string | number;
  marketValue: string | number;
  gainLoss: string | number;
  complete: boolean;
}

export interface Performance {
  range: string;
  points: PerformancePoint[];
  limitedHistory: boolean;
  earliestAvailableDate: string | null;
  limitationMessage: string | null;
}

export interface Transaction {
  id: string;
  portfolioId: string;
  assetType: AssetType;
  stockSymbol: string | null;
  stockExchange: string | null;
  mutualFundSchemeCode: string | null;
  transactionType: TransactionType;
  transactionDate: string;
  quantity: string | number;
  pricePerUnit: string | number | null;
  grossAmount: string | number;
  fees: string | number;
  taxes: string | number;
  netAmount: string | number;
  notes: string | null;
  externalReference: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionInput {
  assetType: AssetType;
  stockSymbol?: string | null;
  stockExchange?: string | null;
  mutualFundSchemeCode?: string | null;
  transactionType: TransactionType;
  transactionDate: string;
  quantity: number;
  pricePerUnit?: number | null;
  grossAmount: number;
  fees?: number;
  taxes?: number;
  notes?: string | null;
  externalReference?: string | null;
}

export interface TransactionFilters {
  assetType?: AssetType;
  symbol?: string;
  transactionType?: TransactionType;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
}
