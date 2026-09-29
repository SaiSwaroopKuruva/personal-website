"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useCreatePortfolio } from "@/hooks/use-portfolio";
import { portfolioSchema, type PortfolioFormValues } from "@/lib/validations/portfolio";

interface PortfolioFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const DEFAULTS: PortfolioFormValues = { name: "", description: "", isDefault: false };

export function PortfolioFormDialog({ open, onOpenChange }: PortfolioFormDialogProps) {
  const form = useForm<PortfolioFormValues>({ resolver: zodResolver(portfolioSchema), defaultValues: DEFAULTS });
  const createPortfolio = useCreatePortfolio();

  const onSubmit = (values: PortfolioFormValues) => {
    createPortfolio.mutate(
      { name: values.name, description: values.description || undefined, isDefault: values.isDefault },
      {
        onSuccess: () => {
          form.reset(DEFAULTS);
          onOpenChange(false);
        },
      }
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create portfolio</DialogTitle>
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
                    <Input placeholder="e.g. Retirement" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Description (optional)</FormLabel>
                  <FormControl>
                    <Input placeholder="Optional notes about this portfolio" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" isLoading={createPortfolio.isPending} className="w-full">
              Create portfolio
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
