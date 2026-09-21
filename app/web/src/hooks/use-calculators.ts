"use client";

import { useMutation } from "@tanstack/react-query";
import { calculatorApi } from "@/lib/api/calculator-api";
import type { LumpsumCalculatorInput, SipCalculatorInput, SwpCalculatorInput } from "@/types/calculator";

export function useSipCalculator() {
  return useMutation({ mutationFn: (input: SipCalculatorInput) => calculatorApi.sip(input) });
}

export function useLumpsumCalculator() {
  return useMutation({ mutationFn: (input: LumpsumCalculatorInput) => calculatorApi.lumpsum(input) });
}

export function useSwpCalculator() {
  return useMutation({ mutationFn: (input: SwpCalculatorInput) => calculatorApi.swp(input) });
}
