import { apiClient } from "@/lib/api/axios-client";
import type { PageResponse } from "@/types/security";
import type {
  Allocation,
  Performance,
  Portfolio,
  PortfolioHoldings,
  PortfolioInput,
  PortfolioSummary,
  Transaction,
  TransactionFilters,
  TransactionInput,
  XirrResult,
} from "@/types/portfolio";

/** Downloads a CSV export using the authenticated axios client (a plain <a href> would skip the auth header). */
async function downloadCsv(url: string, filename: string) {
  const response = await apiClient.get<Blob>(url, { responseType: "blob" });
  const objectUrl = URL.createObjectURL(response.data);
  const link = document.createElement("a");
  link.href = objectUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(objectUrl);
}

export const portfolioApi = {
  list: () => apiClient.get<Portfolio[]>("/api/portfolios").then((res) => res.data),

  create: (input: PortfolioInput) => apiClient.post<Portfolio>("/api/portfolios", input).then((res) => res.data),

  get: (portfolioId: string) => apiClient.get<Portfolio>(`/api/portfolios/${portfolioId}`).then((res) => res.data),

  update: (portfolioId: string, input: PortfolioInput) =>
    apiClient.put<Portfolio>(`/api/portfolios/${portfolioId}`, input).then((res) => res.data),

  remove: (portfolioId: string) => apiClient.delete<void>(`/api/portfolios/${portfolioId}`).then((res) => res.data),

  getSummary: (portfolioId: string) =>
    apiClient.get<PortfolioSummary>(`/api/portfolios/${portfolioId}/summary`).then((res) => res.data),

  getHoldings: (portfolioId: string) =>
    apiClient.get<PortfolioHoldings>(`/api/portfolios/${portfolioId}/holdings`).then((res) => res.data),

  getAllocation: (portfolioId: string) =>
    apiClient.get<Allocation>(`/api/portfolios/${portfolioId}/allocation`).then((res) => res.data),

  getPerformance: (portfolioId: string, range: string) =>
    apiClient
      .get<Performance>(`/api/portfolios/${portfolioId}/performance`, { params: { range } })
      .then((res) => res.data),

  getXirr: (portfolioId: string) =>
    apiClient.get<XirrResult>(`/api/portfolios/${portfolioId}/xirr`).then((res) => res.data),

  listTransactions: (portfolioId: string, filters: TransactionFilters) =>
    apiClient
      .get<PageResponse<Transaction>>(`/api/portfolios/${portfolioId}/transactions`, { params: filters })
      .then((res) => res.data),

  createTransaction: (portfolioId: string, input: TransactionInput) =>
    apiClient.post<Transaction>(`/api/portfolios/${portfolioId}/transactions`, input).then((res) => res.data),

  updateTransaction: (portfolioId: string, transactionId: string, input: TransactionInput) =>
    apiClient
      .put<Transaction>(`/api/portfolios/${portfolioId}/transactions/${transactionId}`, input)
      .then((res) => res.data),

  deleteTransaction: (portfolioId: string, transactionId: string) =>
    apiClient.delete<void>(`/api/portfolios/${portfolioId}/transactions/${transactionId}`).then((res) => res.data),

  exportHoldings: (portfolioId: string) =>
    downloadCsv(`/api/portfolios/${portfolioId}/export/holdings`, "holdings.csv"),

  exportTransactions: (portfolioId: string) =>
    downloadCsv(`/api/portfolios/${portfolioId}/export/transactions`, "transactions.csv"),

  exportSummary: (portfolioId: string) =>
    downloadCsv(`/api/portfolios/${portfolioId}/export/summary`, "portfolio-summary.csv"),
};
