export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export const ROUTES = {
  home: "/",
  login: "/login",
  register: "/register",
  forgotPassword: "/forgot-password",
  resetPassword: "/reset-password",
  verifyEmail: "/verify-email",
  dashboard: "/dashboard",
  profile: "/profile",
  profileEdit: "/profile/edit",
  profileSecurity: "/profile/security",
  profilePreferences: "/profile/preferences",
  profileRisk: "/profile/risk",
  profileAddress: "/profile/address",
  profileDevices: "/profile/devices",
  profileNotifications: "/profile/notifications",
  profileChangePassword: "/profile/change-password",
  profileVerifyEmail: "/profile/verify-email",
  mutualFunds: "/mutual-funds",
  mutualFundCompare: "/mutual-funds/compare",
  mutualFundFavorites: "/mutual-funds/favorites",
  mutualFundDetails: (schemeCode: string) => `/mutual-funds/details?schemeCode=${encodeURIComponent(schemeCode)}`,
  stocks: "/stocks",
  stockDetails: (symbol: string) => `/stocks/details?symbol=${encodeURIComponent(symbol)}`,
  market: "/market",
  calculatorSip: "/calculators/sip",
  calculatorLumpsum: "/calculators/lumpsum",
  calculatorSwp: "/calculators/swp",
} as const;

export const AUTH_STORAGE_KEY = "fin-advisor-auth";
