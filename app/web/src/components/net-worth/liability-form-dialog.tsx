"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { useCreateLiability, useUpdateLiability } from "@/hooks/use-net-worth";
import { liabilitySchema, type LiabilityFormValues } from "@/lib/validations/portfolio";
import type { UserLiability } from "@/types/net-worth";

const CATEGORIES = ["HOME_LOAN", "PERSONAL_LOAN", "VEHICLE_LOAN", "CREDIT_CARD", "OTHER"] as const;
const CATEGORY_LABELS: Record<(typeof CATEGORIES)[number], string> = {
  HOME_LOAN: "Home Loan",
  PERSONAL_LOAN: "Personal Loan",
  VEHICLE_LOAN: "Vehicle Loan",
  CREDIT_CARD: "Credit Card",
  OTHER: "Other",
};

const DEFAULTS: LiabilityFormValues = {
  name: "",
  category: "OTHER",
  currentValue: 0,
  valuationDate: new Date().toISOString().slice(0, 10),
  notes: "",
};

export function LiabilityFormDialog({
  open,
  onOpenChange,
  liability,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  liability?: UserLiability | null;
}) {
  const form = useForm<LiabilityFormValues>({ resolver: zodResolver(liabilitySchema), defaultValues: DEFAULTS });
  const createLiability = useCreateLiability();
  const updateLiability = useUpdateLiability();

  useEffect(() => {
    if (open) {
      form.reset(
        liability
          ? {
              name: liability.name,
              category: liability.category,
              currentValue: Number(liability.currentValue),
              valuationDate: liability.valuationDate,
              notes: liability.notes ?? "",
            }
          : DEFAULTS
      );
    }
  }, [open, liability, form]);

  const onSubmit = (values: LiabilityFormValues) => {
    const input = { ...values, notes: values.notes || undefined };
    const onSuccess = () => onOpenChange(false);
    if (liability) {
      updateLiability.mutate({ liabilityId: liability.id, input }, { onSuccess });
    } else {
      createLiability.mutate(input, { onSuccess });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{liability ? "Edit liability" : "Add liability"}</DialogTitle>
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
                    <Input placeholder="e.g. Home Loan - SBI" {...field} />
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
                  <FormLabel>Outstanding balance (₹)</FormLabel>
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
            <Button type="submit" isLoading={createLiability.isPending || updateLiability.isPending} className="w-full">
              {liability ? "Save changes" : "Add liability"}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
