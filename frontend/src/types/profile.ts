export type Gender = "MALE" | "FEMALE" | "OTHER" | "PREFER_NOT_TO_SAY";
export type KycStatus = "PENDING" | "IN_PROGRESS" | "VERIFIED" | "REJECTED";
export type InvestmentExperience = "BEGINNER" | "INTERMEDIATE" | "EXPERIENCED" | "EXPERT";
export type InvestmentHorizon = "SHORT_TERM" | "MEDIUM_TERM" | "LONG_TERM";
export type UserAccountStatus = "ACTIVE" | "SUSPENDED" | "DEACTIVATED";
export type AddressType = "HOME" | "WORK" | "OTHER";

export interface ProfileResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  mobile: string;
  riskProfile: string;
  dateOfBirth: string | null;
  gender: Gender | null;
  occupation: string | null;
  annualIncome: number | null;
  monthlyExpenses: number | null;
  city: string | null;
  state: string | null;
  country: string | null;
  postalCode: string | null;
  panNumber: string | null;
  aadhaarLastFour: string | null;
  kycStatus: KycStatus;
  emailVerified: boolean;
  mobileVerified: boolean;
  profileCompleted: boolean;
  preferredLanguage: string;
  investmentExperience: InvestmentExperience | null;
  investmentHorizon: InvestmentHorizon | null;
  monthlyInvestmentBudget: number | null;
  profilePicture: string | null;
  lastLogin: string | null;
  status: UserAccountStatus;
  createdAt: string;
}

export interface UpdateProfilePayload {
  firstName: string;
  lastName: string;
  dateOfBirth: string | null;
  gender: Gender | null;
  occupation: string | null;
  annualIncome: number | null;
  monthlyExpenses: number | null;
  panNumber: string | null;
  aadhaarLastFour: string | null;
}

export interface UpdatePreferencesPayload {
  investmentExperience: InvestmentExperience | null;
  investmentHorizon: InvestmentHorizon | null;
  monthlyInvestmentBudget: number | null;
}

export interface UpdateAddressPayload {
  city: string;
  state: string;
  country: string;
  postalCode: string;
}

export interface AddressResponse {
  id: string;
  type: AddressType;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SaveAddressPayload {
  type: AddressType;
  addressLine1: string;
  addressLine2?: string | null;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  isDefault: boolean;
}
