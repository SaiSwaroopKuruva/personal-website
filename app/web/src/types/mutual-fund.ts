export interface MutualFundSummary {
  schemeCode: string;
  isin: string | null;
  schemeName: string;
  shortName: string | null;
  amcCode: string;
  amcName: string;
  category: string;
  subCategory: string | null;
  planType: string;
  optionType: string;
  riskLevel: string;
  nav: string | null;
  navDate: string | null;
  expenseRatio: string | null;
  aum: string | null;
  minimumSip: string | null;
  minimumLumpsum: string | null;
  oneYearReturn: string | null;
  threeYearReturn: string | null;
  fiveYearReturn: string | null;
  favorite: boolean;
}

export interface FundManager {
  name: string;
  designation: string | null;
  experienceYears: number | null;
  joiningDate: string | null;
  bio: string | null;
  active: boolean;
}

export interface FundReturn {
  period: string;
  returnPercentage: string;
  annualized: boolean;
  calculatedAsOf: string;
}

export interface FundHolding {
  securityName: string;
  isin: string | null;
  sector: string | null;
  assetType: string;
  weightPercentage: string;
  quantity: string | null;
  marketValue: string | null;
  asOfDate: string;
}

export interface MutualFundDetails {
  schemeCode: string;
  isin: string | null;
  schemeName: string;
  shortName: string | null;
  amcCode: string;
  amcName: string;
  category: string;
  subCategory: string | null;
  planType: string;
  optionType: string;
  assetClass: string;
  investmentObjective: string | null;
  riskLevel: string;
  benchmark: string | null;
  expenseRatio: string | null;
  exitLoad: string | null;
  minimumLumpsum: string | null;
  minimumSip: string | null;
  aum: string | null;
  nav: string | null;
  navDate: string | null;
  inceptionDate: string | null;
  fundManager: string | null;
  managers: FundManager[];
  returns: FundReturn[];
  topHoldings: FundHolding[];
  holdingsAsOfDate: string | null;
  favorite: boolean;
  disclaimer: string;
}

export interface NavPoint {
  date: string;
  nav: string;
}

export interface NavHistory {
  schemeCode: string;
  interval: string;
  points: NavPoint[];
}

export interface ReturnsResponse {
  schemeCode: string;
  returns: FundReturn[];
  disclaimer: string;
}

export interface AmcOption {
  code: string;
  name: string;
}

export interface FilterMetadata {
  amcs: AmcOption[];
  categories: string[];
  subCategories: string[];
  riskLevels: string[];
  planTypes: string[];
  optionTypes: string[];
  assetClasses: string[];
}

export interface FavoriteStatus {
  schemeCode: string;
  favorite: boolean;
}

export interface FavoriteFund {
  fund: MutualFundSummary;
  favoritedAt: string;
}

export interface ComparisonFund {
  schemeCode: string;
  schemeName: string;
  amcName: string;
  category: string;
  subCategory: string | null;
  riskLevel: string;
  nav: string | null;
  aum: string | null;
  expenseRatio: string | null;
  oneYearReturn: string | null;
  threeYearReturn: string | null;
  fiveYearReturn: string | null;
  sinceInceptionReturn: string | null;
  minimumSip: string | null;
  minimumLumpsum: string | null;
  exitLoad: string | null;
  benchmark: string | null;
  fundManager: string | null;
}

export interface ComparisonResult {
  funds: ComparisonFund[];
  disclaimer: string;
}

export interface MutualFundSearchParams {
  search?: string;
  amc?: string;
  category?: string;
  subCategory?: string;
  planType?: string;
  optionType?: string;
  riskLevel?: string;
  minAum?: number;
  maxAum?: number;
  minExpenseRatio?: number;
  maxExpenseRatio?: number;
  sort?: string;
  direction?: "ASC" | "DESC";
  page?: number;
  size?: number;
}
