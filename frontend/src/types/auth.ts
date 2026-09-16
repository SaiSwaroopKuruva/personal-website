export type RiskProfile = "CONSERVATIVE" | "MODERATE" | "AGGRESSIVE";

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  mobile: string;
  riskProfile: RiskProfile;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: User;
}

export interface RegisterPayload {
  firstName: string;
  lastName: string;
  email: string;
  mobile: string;
  password: string;
  riskProfile?: RiskProfile;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}
