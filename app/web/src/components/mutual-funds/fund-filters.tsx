"use client";

import { RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { useMutualFundFilters } from "@/hooks/use-mutual-funds";
import type { MutualFundSearchParams } from "@/types/mutual-fund";

export interface FundFilterValues
  extends Pick<
    MutualFundSearchParams,
    "amc" | "category" | "subCategory" | "planType" | "optionType" | "riskLevel" | "minExpenseRatio" | "maxExpenseRatio" | "minAum" | "maxAum"
  > {}

interface FundFiltersProps {
  values: FundFilterValues;
  onChange: (values: FundFilterValues) => void;
  onReset: () => void;
}

export function FundFilters({ values, onChange, onReset }: FundFiltersProps) {
  const { data: filters, isLoading } = useMutualFundFilters();

  const update = <K extends keyof FundFilterValues>(key: K, value: FundFilterValues[K]) => {
    onChange({ ...values, [key]: value });
  };

  if (isLoading || !filters) {
    return (
      <div className="space-y-4">
        {Array.from({ length: 5 }).map((_, i) => (
          <Skeleton key={i} className="h-10 w-full" />
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold">Filters</h2>
        <Button variant="ghost" size="sm" onClick={onReset}>
          <RotateCcw className="h-3.5 w-3.5" /> Reset
        </Button>
      </div>

      <SelectField
        id="amc-filter"
        label="AMC"
        value={values.amc ?? ""}
        onChange={(v) => update("amc", v || undefined)}
        options={filters.amcs.map((amc) => ({ label: amc.name, value: amc.code }))}
      />

      <SelectField
        id="category-filter"
        label="Category"
        value={values.category ?? ""}
        onChange={(v) => update("category", v || undefined)}
        options={filters.categories.map((c) => ({ label: c, value: c }))}
      />

      <SelectField
        id="sub-category-filter"
        label="Sub-category"
        value={values.subCategory ?? ""}
        onChange={(v) => update("subCategory", v || undefined)}
        options={filters.subCategories.map((c) => ({ label: c, value: c }))}
      />

      <fieldset className="space-y-2">
        <legend className="text-xs font-medium text-muted-foreground">Risk Level</legend>
        <div className="flex flex-wrap gap-2">
          {filters.riskLevels.map((risk) => (
            <button
              key={risk}
              type="button"
              aria-pressed={values.riskLevel === risk}
              onClick={() => update("riskLevel", values.riskLevel === risk ? undefined : risk)}
              className={`rounded-full border px-3 py-1 text-xs font-medium transition-colors ${
                values.riskLevel === risk
                  ? "border-primary bg-primary text-primary-foreground"
                  : "border-input bg-background hover:bg-accent"
              }`}
            >
              {risk.replace(/_/g, " ")}
            </button>
          ))}
        </div>
      </fieldset>

      <SelectField
        id="plan-type-filter"
        label="Plan Type"
        value={values.planType ?? ""}
        onChange={(v) => update("planType", v || undefined)}
        options={filters.planTypes.map((c) => ({ label: c, value: c }))}
      />

      <SelectField
        id="option-type-filter"
        label="Option Type"
        value={values.optionType ?? ""}
        onChange={(v) => update("optionType", v || undefined)}
        options={filters.optionTypes.map((c) => ({ label: c, value: c }))}
      />

      <div className="space-y-2">
        <Label>Expense Ratio (%)</Label>
        <div className="flex items-center gap-2">
          <NumberInput
            aria-label="Minimum expense ratio"
            value={values.minExpenseRatio}
            onChange={(v) => update("minExpenseRatio", v)}
            placeholder="Min"
          />
          <span className="text-muted-foreground">–</span>
          <NumberInput
            aria-label="Maximum expense ratio"
            value={values.maxExpenseRatio}
            onChange={(v) => update("maxExpenseRatio", v)}
            placeholder="Max"
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label>AUM (₹ Crore)</Label>
        <div className="flex items-center gap-2">
          <NumberInput aria-label="Minimum AUM" value={values.minAum} onChange={(v) => update("minAum", v)} placeholder="Min" />
          <span className="text-muted-foreground">–</span>
          <NumberInput aria-label="Maximum AUM" value={values.maxAum} onChange={(v) => update("maxAum", v)} placeholder="Max" />
        </div>
      </div>
    </div>
  );
}

function SelectField({
  id,
  label,
  value,
  onChange,
  options,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: { label: string; value: string }[];
}) {
  return (
    <div className="space-y-1.5">
      <Label htmlFor={id}>{label}</Label>
      <select
        id={id}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      >
        <option value="">All</option>
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}

function NumberInput({
  value,
  onChange,
  placeholder,
  ...props
}: {
  value: number | undefined;
  onChange: (value: number | undefined) => void;
  placeholder: string;
} & Omit<React.InputHTMLAttributes<HTMLInputElement>, "value" | "onChange" | "placeholder">) {
  return (
    <input
      type="number"
      inputMode="decimal"
      value={value ?? ""}
      placeholder={placeholder}
      onChange={(e) => onChange(e.target.value === "" ? undefined : Number(e.target.value))}
      className="h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      {...props}
    />
  );
}
