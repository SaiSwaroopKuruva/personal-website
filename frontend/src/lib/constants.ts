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
} as const;

export const AUTH_STORAGE_KEY = "fin-advisor-auth";
