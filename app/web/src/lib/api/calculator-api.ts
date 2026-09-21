import { apiClient } from "@/lib/api/axios-client";
import type {
  LumpsumCalculatorInput,
  LumpsumCalculatorResult,
  SipCalculatorInput,
  SipCalculatorResult,
  SwpCalculatorInput,
  SwpCalculatorResult,
} from "@/types/calculator";

export const calculatorApi = {
  sip: (input: SipCalculatorInput) =>
    apiClient.post<SipCalculatorResult>("/api/calculators/sip", input).then((res) => res.data),

  lumpsum: (input: LumpsumCalculatorInput) =>
    apiClient.post<LumpsumCalculatorResult>("/api/calculators/lumpsum", input).then((res) => res.data),

  swp: (input: SwpCalculatorInput) =>
    apiClient.post<SwpCalculatorResult>("/api/calculators/swp", input).then((res) => res.data),
};
