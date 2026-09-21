import { z } from "zod";

const positiveAmount = z.coerce.number({ invalid_type_error: "Enter a valid amount" }).positive("Must be greater than 0");
const returnPercent = z.coerce
  .number({ invalid_type_error: "Enter a valid percentage" })
  .min(0, "Must be 0 or more")
  .max(30, "Must be 30% or less");
const durationYears = z.coerce
  .number({ invalid_type_error: "Enter a valid duration" })
  .int("Must be a whole number of years")
  .positive("Must be greater than 0")
  .max(50, "Must be 50 years or less");

export const sipCalculatorSchema = z.object({
  monthlyInvestment: positiveAmount.max(10000000, "Must be ₹1,00,00,000 or less"),
  expectedAnnualReturn: returnPercent,
  durationYears,
});
export type SipCalculatorFormValues = z.infer<typeof sipCalculatorSchema>;

export const lumpsumCalculatorSchema = z.object({
  principal: positiveAmount.max(100000000, "Must be ₹10,00,00,000 or less"),
  expectedAnnualReturn: returnPercent,
  durationYears,
});
export type LumpsumCalculatorFormValues = z.infer<typeof lumpsumCalculatorSchema>;

export const swpCalculatorSchema = z.object({
  initialInvestment: positiveAmount.max(100000000, "Must be ₹10,00,00,000 or less"),
  withdrawalPerMonth: z.coerce
    .number({ invalid_type_error: "Enter a valid amount" })
    .min(0, "Must be 0 or more")
    .max(10000000, "Must be ₹1,00,00,000 or less"),
  expectedAnnualReturn: returnPercent,
  durationYears,
});
export type SwpCalculatorFormValues = z.infer<typeof swpCalculatorSchema>;
