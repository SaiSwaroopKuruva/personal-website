"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { AmountBarChart } from "@/components/calculators/amount-bar-chart";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";
import { useSipCalculator } from "@/hooks/use-calculators";
import { sipCalculatorSchema, type SipCalculatorFormValues } from "@/lib/validations/calculator";
import { formatInr } from "@/lib/utils";

const DEFAULTS: SipCalculatorFormValues = { monthlyInvestment: 10000, expectedAnnualReturn: 12, durationYears: 10 };

export default function SipCalculatorPage() {
  const form = useForm<SipCalculatorFormValues>({ resolver: zodResolver(sipCalculatorSchema), defaultValues: DEFAULTS });
  const calculate = useSipCalculator();

  const onSubmit = (values: SipCalculatorFormValues) => calculate.mutate(values);
  const onReset = () => {
    form.reset(DEFAULTS);
    calculate.reset();
  };

  const result = calculate.data;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">SIP Calculator</h1>
        <p className="text-sm text-muted-foreground">Estimate the future value of a monthly Systematic Investment Plan.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Investment Details</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
              <FormField
                control={form.control}
                name="monthlyInvestment"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Monthly Investment (₹)</FormLabel>
                    <FormControl>
                      <Input type="number" min={1} step="100" {...field} />
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
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Stat label="Total Investment" value={formatInr(result.totalInvestment)} />
              <Stat label="Estimated Returns" value={formatInr(result.estimatedReturns)} accent />
              <Stat label="Future Value" value={formatInr(result.futureValue)} large />
            </div>
            <AmountBarChart
              segments={[
                { label: "Invested", value: Number(result.totalInvestment), className: "bg-primary" },
                { label: "Estimated Returns", value: Number(result.estimatedReturns), className: "bg-emerald-500" },
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
