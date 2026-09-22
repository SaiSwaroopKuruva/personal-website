export interface ProviderStatus {
  provider: string;
  status: "CONNECTED" | "DEGRADED" | "UNAVAILABLE" | "DISABLED";
  lastSuccessfulUpdate: string | null;
  dataType: string | null;
  message: string | null;
}

export interface ProviderStatusResponse {
  stocks: ProviderStatus;
  mutualFunds: ProviderStatus;
}
