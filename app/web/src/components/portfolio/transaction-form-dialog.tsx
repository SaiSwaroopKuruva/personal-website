"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { useCreateTransaction, useUpdateTransaction } from "@/hooks/use-portfolio";
import { transactionSchema, type TransactionFormValues } from "@/lib/validations/portfolio";
import type { Transaction, TransactionInput } from "@/types/portfolio";

interface TransactionFormDialogProps {
  portfolioId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  transaction?: Transaction | null;
}

const STOCK_TRANSACTION_TYPES = ["BUY", "SELL", "DIVIDEND", "FEE", "TAX", "ADJUSTMENT"] as const;
const MUTUAL_FUND_TRANSACTION_TYPES = ["PURCHASE", "REDEMPTION", "DIVIDEND", "FEE", "ADJUSTMENT"] as const;

const DEFAULTS: TransactionFormValues = {
  assetType: "STOCK",
  stockSymbol: "",
  stockExchange: "NSE",
  mutualFundSchemeCode: "",
  transactionType: "BUY",
  transactionDate: new Date().toISOString().slice(0, 10),
  quantity: 0,
  pricePerUnit: undefined,
  grossAmount: 0,
  fees: 0,
  taxes: 0,
  notes: "",
  externalReference: "",
};

export function TransactionFormDialog({ portfolioId, open, onOpenChange, transaction }: TransactionFormDialogProps) {
  const form = useForm<TransactionFormValues>({ resolver: zodResolver(transactionSchema), defaultValues: DEFAULTS });
  const createTransaction = useCreateTransaction(portfolioId);
  const updateTransaction = useUpdateTransaction(portfolioId);
  const assetType = form.watch("assetType");

  useEffect(() => {
    if (open) {
      form.reset(
        transaction
          ? {
              assetType: transaction.assetType,
              stockSymbol: transaction.stockSymbol ?? "",
              stockExchange: transaction.stockExchange ?? "NSE",
              mutualFundSchemeCode: transaction.mutualFundSchemeCode ?? "",
              transactionType: transaction.transactionType,
              transactionDate: transaction.transactionDate,
              quantity: Number(transaction.quantity),
              pricePerUnit: transaction.pricePerUnit !== null ? Number(transaction.pricePerUnit) : undefined,
              grossAmount: Number(transaction.grossAmount),
              fees: Number(transaction.fees),
              taxes: Number(transaction.taxes),
              notes: transaction.notes ?? "",
              externalReference: transaction.externalReference ?? "",
            }
          : DEFAULTS
      );
    }
  }, [open, transaction, form]);

  const onSubmit = (values: TransactionFormValues) => {
    const input: TransactionInput = {
      assetType: values.assetType,
      stockSymbol: values.assetType === "STOCK" ? values.stockSymbol?.toUpperCase() : undefined,
      stockExchange: values.assetType === "STOCK" ? values.stockExchange : undefined,
      mutualFundSchemeCode: values.assetType === "MUTUAL_FUND" ? values.mutualFundSchemeCode : undefined,
      transactionType: values.transactionType,
      transactionDate: values.transactionDate,
      quantity: values.quantity,
      pricePerUnit: values.pricePerUnit,
      grossAmount: values.grossAmount,
      fees: values.fees ?? 0,
      taxes: values.taxes ?? 0,
      notes: values.notes || undefined,
      externalReference: values.externalReference || undefined,
    };

    const onSuccess = () => onOpenChange(false);
    if (transaction) {
      updateTransaction.mutate({ transactionId: transaction.id, input }, { onSuccess });
    } else {
      createTransaction.mutate(input, { onSuccess });
    }
  };

  const transactionTypeOptions = assetType === "STOCK" ? STOCK_TRANSACTION_TYPES : MUTUAL_FUND_TRANSACTION_TYPES;
  const isPending = createTransaction.isPending || updateTransaction.isPending;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{transaction ? "Edit transaction" : "Record transaction"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="assetType"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Asset type</FormLabel>
                    <FormControl>
                      <Select {...field}>
                        <option value="STOCK">Stock</option>
                        <option value="MUTUAL_FUND">Mutual fund</option>
                      </Select>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="transactionType"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Transaction type</FormLabel>
                    <FormControl>
                      <Select {...field}>
                        {transactionTypeOptions.map((type) => (
                          <option key={type} value={type}>
                            {type}
                          </option>
                        ))}
                      </Select>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            {assetType === "STOCK" ? (
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="stockSymbol"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Symbol</FormLabel>
                      <FormControl>
                        <Input placeholder="e.g. INFY" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="stockExchange"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Exchange</FormLabel>
                      <FormControl>
                        <Select {...field}>
                          <option value="NSE">NSE</option>
                          <option value="BSE">BSE</option>
                        </Select>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
            ) : (
              <FormField
                control={form.control}
                name="mutualFundSchemeCode"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>AMFI scheme code</FormLabel>
                    <FormControl>
                      <Input placeholder="e.g. 119551" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}

            <FormField
              control={form.control}
              name="transactionDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Transaction date</FormLabel>
                  <FormControl>
                    <Input type="date" max={new Date().toISOString().slice(0, 10)} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="quantity"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Units</FormLabel>
                    <FormControl>
                      <Input type="number" step="any" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="pricePerUnit"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Price/unit (optional)</FormLabel>
                    <FormControl>
                      <Input type="number" step="any" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="grossAmount"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Gross amount (₹)</FormLabel>
                  <FormControl>
                    <Input type="number" step="any" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="fees"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Fees (₹)</FormLabel>
                    <FormControl>
                      <Input type="number" step="any" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="taxes"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Taxes (₹)</FormLabel>
                    <FormControl>
                      <Input type="number" step="any" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="notes"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Notes (optional)</FormLabel>
                  <FormControl>
                    <Input placeholder="Optional notes" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" isLoading={isPending} className="w-full">
              {transaction ? "Save changes" : "Record transaction"}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
