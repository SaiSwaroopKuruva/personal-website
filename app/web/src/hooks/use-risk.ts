"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { riskApi } from "@/lib/api/risk-api";
import type { RiskAnswer } from "@/types/risk";

export function useRiskQuestions() {
  return useQuery({ queryKey: ["risk", "questions"], queryFn: riskApi.getQuestions });
}

export function useLatestRiskAssessment() {
  return useQuery({
    queryKey: ["risk", "latest"],
    queryFn: riskApi.getLatest,
    retry: false,
  });
}

export function useRiskHistory(page = 0, size = 20) {
  return useQuery({
    queryKey: ["risk", "history", page, size],
    queryFn: () => riskApi.getHistory(page, size),
  });
}

export function useSubmitRiskAssessment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (answers: RiskAnswer[]) => riskApi.submit(answers),
    onSuccess: (data) => {
      queryClient.setQueryData(["risk", "latest"], data);
      queryClient.invalidateQueries({ queryKey: ["risk", "history"] });
      queryClient.invalidateQueries({ queryKey: ["profile"] });
      toast.success("Your investor risk profile has been updated");
    },
    onError: () => toast.error("Could not submit your risk assessment. Please try again."),
  });
}
