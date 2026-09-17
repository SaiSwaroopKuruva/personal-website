"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { useProfile, useUpdateLanguage, useUpdatePreferences } from "@/hooks/use-profile";
import { preferencesSchema, type PreferencesFormValues } from "@/lib/validations/profile";

const LANGUAGES = [
  { code: "en", label: "English" },
  { code: "hi", label: "Hindi" },
  { code: "mr", label: "Marathi" },
  { code: "ta", label: "Tamil" },
  { code: "te", label: "Telugu" },
];

export default function PreferencesPage() {
  const { data: profile, isLoading } = useProfile();
  const updatePreferences = useUpdatePreferences();
  const updateLanguage = useUpdateLanguage();

  const form = useForm<PreferencesFormValues>({
    resolver: zodResolver(preferencesSchema),
  });

  useEffect(() => {
    if (profile) {
      form.reset({
        investmentExperience: profile.investmentExperience ?? undefined,
        investmentHorizon: profile.investmentHorizon ?? undefined,
        monthlyInvestmentBudget: profile.monthlyInvestmentBudget ?? undefined,
      });
    }
  }, [profile, form]);

  if (isLoading || !profile) {
    return <Skeleton className="h-80" />;
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Investor Preferences</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form
              onSubmit={form.handleSubmit((values) => updatePreferences.mutate({
                investmentExperience: values.investmentExperience ?? null,
                investmentHorizon: values.investmentHorizon ?? null,
                monthlyInvestmentBudget: values.monthlyInvestmentBudget ?? null,
              }))}
              className="grid gap-4 sm:grid-cols-2"
            >
              <FormField
                control={form.control}
                name="investmentExperience"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Investment experience</FormLabel>
                    <FormControl>
                      <select {...field} value={field.value ?? ""} className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm shadow-sm">
                        <option value="">Select</option>
                        <option value="BEGINNER">Beginner</option>
                        <option value="INTERMEDIATE">Intermediate</option>
                        <option value="EXPERIENCED">Experienced</option>
                        <option value="EXPERT">Expert</option>
                      </select>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="investmentHorizon"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Investment horizon</FormLabel>
                    <FormControl>
                      <select {...field} value={field.value ?? ""} className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm shadow-sm">
                        <option value="">Select</option>
                        <option value="SHORT_TERM">Short term</option>
                        <option value="MEDIUM_TERM">Medium term</option>
                        <option value="LONG_TERM">Long term</option>
                      </select>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="monthlyInvestmentBudget"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Monthly investment budget (₹)</FormLabel>
                    <FormControl>
                      <Input type="number" min="0" {...field} value={field.value ?? ""} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className="sm:col-span-2">
                <Button type="submit" isLoading={updatePreferences.isPending}>
                  Save preferences
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Preferred Language</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-2">
          {LANGUAGES.map((language) => (
            <Button
              key={language.code}
              variant={profile.preferredLanguage === language.code ? "default" : "outline"}
              size="sm"
              onClick={() => updateLanguage.mutate(language.code)}
              disabled={updateLanguage.isPending}
            >
              {language.label}
            </Button>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
