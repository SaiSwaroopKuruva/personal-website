"use client";

import Link from "next/link";
import { Scale, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useCompareStore, MAX_COMPARE_FUNDS } from "@/store/compare-store";
import { ROUTES } from "@/lib/constants";

export function CompareBar() {
  const { schemeCodes, remove, clear } = useCompareStore();

  if (schemeCodes.length === 0) return null;

  return (
    <div className="fixed inset-x-0 bottom-0 z-30 border-t border-border bg-background/95 p-3 shadow-lg backdrop-blur">
      <div className="container flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <span className="flex items-center gap-1.5 text-sm font-medium">
            <Scale className="h-4 w-4" /> {schemeCodes.length}/{MAX_COMPARE_FUNDS} selected
          </span>
          {schemeCodes.map((code) => (
            <span key={code} className="flex items-center gap-1 rounded-full bg-secondary px-2.5 py-1 text-xs">
              {code}
              <button aria-label={`Remove ${code} from comparison`} onClick={() => remove(code)}>
                <X className="h-3 w-3" />
              </button>
            </span>
          ))}
        </div>
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="sm" onClick={clear}>
            Clear
          </Button>
          <Button size="sm" disabled={schemeCodes.length < 2} asChild={schemeCodes.length >= 2}>
            {schemeCodes.length >= 2 ? <Link href={ROUTES.mutualFundCompare}>Compare Now</Link> : <span>Compare Now</span>}
          </Button>
        </div>
      </div>
    </div>
  );
}
