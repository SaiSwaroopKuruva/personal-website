import { apiClient } from "@/lib/api/axios-client";
import type { PageResponse } from "@/types/security";
import type { RiskAnswer, RiskAssessmentResponse, RiskQuestion } from "@/types/risk";

export const riskApi = {
  getQuestions: () => apiClient.get<RiskQuestion[]>("/api/risk/questions").then((res) => res.data),

  submit: (answers: RiskAnswer[]) =>
    apiClient.post<RiskAssessmentResponse>("/api/risk/submit", { answers }).then((res) => res.data),

  getLatest: () => apiClient.get<RiskAssessmentResponse>("/api/risk/latest").then((res) => res.data),

  getHistory: (page = 0, size = 20) =>
    apiClient
      .get<PageResponse<RiskAssessmentResponse>>("/api/risk/history", { params: { page, size } })
      .then((res) => res.data),
};
