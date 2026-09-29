"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { useCreateAsset, useUpdateAsset } from "@/hooks/use-net-worth";
import { assetSchema, type AssetFormValues } from "@/lib/validations/portfolio";
import type { UserAsset } from "@/types/net-worth";

const CATEGORIES = ["CASH_AND_SAVINGS", "FIXED_DEPOSIT", "OTHER_INVESTMENT", "PROPERTY", "GOLD", "OTHER"] as const;
const CATEGORY_LABELS: Record<(typeof CATEGORIES)[number], string> = {
  CASH_AND_SAVINGS: "Cash & Savings",
  FIXED_DEPOSIT: "Fixed Deposit",
  OTHER_INVESTMENT: "Other Investment",
  PROPERTY: "Property",
  GOLD: "Gold",
  OTHER: "Other",
};

const DEFAULTS: AssetFormValues = {
  name: "",
  category: "CASH_AND_SAVINGS",
  currentValue: 0,
  valuationDate: new Date().toISOString().slice(0, 10),
  notes: "",
};

export function AssetFormDialog({ open, onOpenChange, asset }: { open: boolean; onOpenChange: (open: boolean) => void; asset?: UserAsset | null }) {
  const form = useForm<AssetFormValues>({ resolver: zodResolver(assetSchema), defaultValues: DEFAULTS });
  const createAsset = useCreateAsset();
  const updateAsset = useUpdateAsset();

  useEffect(() => {
    if (open) {
      form.reset(
        asset
          ? {
              name: asset.name,
              category: asset.category,
              currentValue: Number(asset.currentValue),
              valuationDate: asset.valuationDate,
              notes: asset.notes ?? "",
            }
          : DEFAULTS
      );
    }
  }, [open, asset, form]);

  const onSubmit = (values: AssetFormValues) => {
    const input = { ...values, notes: values.notes || undefined };
    const onSuccess = () => onOpenChange(false);
    if (asset) {
      updateAsset.mutate({ assetId: asset.id, input }, { onSuccess });
    } else {
      createAsset.mutate(input, { onSuccess });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{asset ? "Edit asset" : "Add asset"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input placeholder="e.g. HDFC Savings Account" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="category"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Category</FormLabel>
                  <FormControl>
                    <Select {...field}>
                      {CATEGORIES.map((c) => (
                        <option key={c} value={c}>
                          {CATEGORY_LABELS[c]}
                        </option>
                      ))}
                    </Select>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="currentValue"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Current value (₹)</FormLabel>
                  <FormControl>
                    <Input type="number" step="any" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="valuationDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>As of date</FormLabel>
                  <FormControl>
                    <Input type="date" max={new Date().toISOString().slice(0, 10)} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" isLoading={createAsset.isPending || updateAsset.isPending} className="w-full">
              {asset ? "Save changes" : "Add asset"}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
