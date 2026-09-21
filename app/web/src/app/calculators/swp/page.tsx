"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { AmountBarChart } from "@/components/calculators/amount-bar-chart";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { useSwpCalculator } from "@/hooks/use-calculators";
import { swpCalculatorSchema, type SwpCalculatorFormValues } from "@/lib/validations/calculator";
import { formatInr } from "@/lib/utils";

const DEFAULTS: SwpCalculatorFormValues = {
  initialInvestment: 1000000,
  withdrawalPerMonth: 8000,
  expectedAnnualReturn: 8,
  durationYears: 10,
};

export default function SwpCalculatorPage() {
  const form = useForm<SwpCalculatorFormValues>({ resolver: zodResolver(swpCalculatorSchema), defaultValues: DEFAULTS });
  const calculate = useSwpCalculator();

  const onSubmit = (values: SwpCalculatorFormValues) => calculate.mutate(values);
  const onReset = () => {
    form.reset(DEFAULTS);
    calculate.reset();
  };

  const result = calculate.data;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">SWP Calculator</h1>
        <p className="text-sm text-muted-foreground">Estimate a Systematic Withdrawal Plan from an existing investment.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Withdrawal Plan Details</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
              <FormField
                control={form.control}
                name="initialInvestment"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Initial Investment (₹)</FormLabel>
                    <FormControl>
                      <Input type="number" min={1} step="1000" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="withdrawalPerMonth"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Monthly Withdrawal (₹)</FormLabel>
                    <FormControl>
                      <Input type="number" min={0} step="500" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="expectedAnnualReturn"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Expected Annual Return (%)</FormLabel>
                    <FormControl>
                      <Input type="number" min={0} max={30} step="0.1" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="durationYears"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Duration (Years)</FormLabel>
                    <FormControl>
                      <Input type="number" min={1} max={50} step="1" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className="flex gap-2">
                <Button type="submit" isLoading={calculate.isPending}>
                  Calculate
                </Button>
                <Button type="button" variant="outline" onClick={onReset}>
                  Reset
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>

      {result && (
        <Card>
          <CardHeader>
            <CardTitle>Results (Estimate)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {result.exhausted && (
              <Badge variant="destructive">
                Corpus exhausted after {result.exhaustedAfterMonths} month{result.exhaustedAfterMonths === 1 ? "" : "s"}
              </Badge>
            )}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Stat label="Total Withdrawn" value={formatInr(result.totalWithdrawn)} />
              <Stat label="Remaining Value" value={formatInr(result.remainingValue)} large />
              <Stat
                label="Estimated Growth"
                value={formatInr(result.estimatedGrowth)}
                accent={Number(result.estimatedGrowth) >= 0}
              />
            </div>
            <AmountBarChart
              segments={[
                { label: "Withdrawn", value: Number(result.totalWithdrawn), className: "bg-amber-500" },
                { label: "Remaining", value: Number(result.remainingValue), className: "bg-primary" },
              ]}
            />
            <Disclaimer text={result.disclaimer} />
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function Stat({ label, value, accent, large }: { label: string; value: string; accent?: boolean; large?: boolean }) {
  return (
    <div className="rounded-lg border border-border p-3">
      <p className="text-[11px] uppercase tracking-wide text-muted-foreground">{label}</p>
      <p className={`font-semibold ${large ? "text-xl" : "text-lg"} ${accent ? "text-emerald-600 dark:text-emerald-400" : ""}`}>{value}</p>
    </div>
  );
}
