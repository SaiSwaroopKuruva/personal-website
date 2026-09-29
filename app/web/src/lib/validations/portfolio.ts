import { z } from "zod";

export const portfolioSchema = z.object({
  name: z.string().min(1, "Name is required").max(150, "Must be 150 characters or fewer"),
  description: z.string().max(1000, "Must be 1000 characters or fewer").optional().or(z.literal("")),
  isDefault: z.boolean().optional(),
});
export type PortfolioFormValues = z.infer<typeof portfolioSchema>;

const ASSET_TYPES = ["STOCK", "MUTUAL_FUND"] as const;
const TRANSACTION_TYPES = ["BUY", "SELL", "PURCHASE", "REDEMPTION", "DIVIDEND", "FEE", "TAX", "ADJUSTMENT"] as const;

export const transactionSchema = z
  .object({
    assetType: z.enum(ASSET_TYPES),
    stockSymbol: z.string().max(50).optional().or(z.literal("")),
    stockExchange: z.string().max(20).optional().or(z.literal("")),
    mutualFundSchemeCode: z.string().max(50).optional().or(z.literal("")),
    transactionType: z.enum(TRANSACTION_TYPES),
    transactionDate: z.string().min(1, "Date is required"),
    quantity: z.coerce.number({ invalid_type_error: "Enter a valid quantity" }),
    pricePerUnit: z.coerce.number().optional(),
    grossAmount: z.coerce.number({ invalid_type_error: "Enter a valid amount" }).min(0, "Must be 0 or more"),
    fees: z.coerce.number().min(0, "Must be 0 or more").optional(),
    taxes: z.coerce.number().min(0, "Must be 0 or more").optional(),
    notes: z.string().max(1000).optional().or(z.literal("")),
    externalReference: z.string().max(100).optional().or(z.literal("")),
  })
  .superRefine((data, ctx) => {
    if (data.assetType === "STOCK" && !data.stockSymbol) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["stockSymbol"], message: "Stock symbol is required" });
    }
    if (data.assetType === "MUTUAL_FUND" && !data.mutualFundSchemeCode) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["mutualFundSchemeCode"], message: "Scheme code is required" });
    }
    if (data.transactionType !== "ADJUSTMENT" && data.quantity < 0) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["quantity"], message: "Only adjustments may be negative" });
    }
  });
export type TransactionFormValues = z.infer<typeof transactionSchema>;

const ASSET_CATEGORIES = ["CASH_AND_SAVINGS", "FIXED_DEPOSIT", "OTHER_INVESTMENT", "PROPERTY", "GOLD", "OTHER"] as const;
const LIABILITY_CATEGORIES = ["HOME_LOAN", "PERSONAL_LOAN", "VEHICLE_LOAN", "CREDIT_CARD", "OTHER"] as const;

export const assetSchema = z.object({
  name: z.string().min(1, "Name is required").max(150),
  category: z.enum(ASSET_CATEGORIES),
  currentValue: z.coerce.number({ invalid_type_error: "Enter a valid amount" }).min(0, "Must be 0 or more"),
  valuationDate: z.string().min(1, "Date is required"),
  notes: z.string().max(1000).optional().or(z.literal("")),
});
export type AssetFormValues = z.infer<typeof assetSchema>;

export const liabilitySchema = z.object({
  name: z.string().min(1, "Name is required").max(150),
  category: z.enum(LIABILITY_CATEGORIES),
  currentValue: z.coerce.number({ invalid_type_error: "Enter a valid amount" }).min(0, "Must be 0 or more"),
  valuationDate: z.string().min(1, "Date is required"),
  notes: z.string().max(1000).optional().or(z.literal("")),
});
export type LiabilityFormValues = z.infer<typeof liabilitySchema>;
