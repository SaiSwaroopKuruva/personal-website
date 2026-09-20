import { z } from "zod";

const PAN_REGEX = /^[A-Z]{5}[0-9]{4}[A-Z]{1}$/;
const AADHAAR_LAST_FOUR_REGEX = /^[0-9]{4}$/;
const POSTAL_CODE_REGEX = /^[1-9][0-9]{5}$/;

export const updateProfileSchema = z.object({
  firstName: z.string().min(1, "First name is required").max(100),
  lastName: z.string().min(1, "Last name is required").max(100),
  dateOfBirth: z.string().optional().or(z.literal("")),
  gender: z.enum(["MALE", "FEMALE", "OTHER", "PREFER_NOT_TO_SAY"]).optional(),
  occupation: z.string().max(100).optional().or(z.literal("")),
  annualIncome: z.coerce.number().min(0, "Annual income cannot be negative").optional(),
  monthlyExpenses: z.coerce.number().min(0, "Monthly expenses cannot be negative").optional(),
  panNumber: z
    .string()
    .regex(PAN_REGEX, "PAN number must match the format AAAAA9999A")
    .optional()
    .or(z.literal("")),
  aadhaarLastFour: z
    .string()
    .regex(AADHAAR_LAST_FOUR_REGEX, "Aadhaar last four must be exactly 4 digits")
    .optional()
    .or(z.literal("")),
});

export type UpdateProfileFormValues = z.infer<typeof updateProfileSchema>;

export const updateAddressSchema = z.object({
  city: z.string().min(1, "City is required").max(100),
  state: z.string().min(1, "State is required").max(100),
  country: z.string().min(1, "Country is required").max(100),
  postalCode: z.string().regex(POSTAL_CODE_REGEX, "Enter a valid 6-digit PIN code"),
});

export type UpdateAddressFormValues = z.infer<typeof updateAddressSchema>;

export const saveAddressSchema = z.object({
  type: z.enum(["HOME", "WORK", "OTHER"]),
  addressLine1: z.string().min(1, "Address line 1 is required").max(255),
  addressLine2: z.string().max(255).optional().or(z.literal("")),
  city: z.string().min(1, "City is required").max(100),
  state: z.string().min(1, "State is required").max(100),
  country: z.string().min(1, "Country is required").max(100),
  postalCode: z.string().regex(POSTAL_CODE_REGEX, "Enter a valid 6-digit PIN code"),
  isDefault: z.boolean(),
});

export type SaveAddressFormValues = z.infer<typeof saveAddressSchema>;

export const preferencesSchema = z.object({
  investmentExperience: z.enum(["BEGINNER", "INTERMEDIATE", "EXPERIENCED", "EXPERT"]).optional(),
  investmentHorizon: z.enum(["SHORT_TERM", "MEDIUM_TERM", "LONG_TERM"]).optional(),
  monthlyInvestmentBudget: z.coerce.number().min(0, "Budget cannot be negative").optional(),
});

export type PreferencesFormValues = z.infer<typeof preferencesSchema>;
