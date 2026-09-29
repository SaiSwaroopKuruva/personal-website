"use client";

import { useState } from "react";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { PortfolioFormDialog } from "@/components/portfolio/portfolio-form-dialog";
import type { Portfolio } from "@/types/portfolio";

interface PortfolioSelectorProps {
  portfolios: Portfolio[];
  selectedPortfolioId: string | null;
  onSelect: (portfolioId: string) => void;
}

export function PortfolioSelector({ portfolios, selectedPortfolioId, onSelect }: PortfolioSelectorProps) {
  const [dialogOpen, setDialogOpen] = useState(false);

  if (portfolios.length === 0) {
    return (
      <>
        <Button size="sm" onClick={() => setDialogOpen(true)}>
          <Plus className="h-4 w-4" /> Create portfolio
        </Button>
        <PortfolioFormDialog open={dialogOpen} onOpenChange={setDialogOpen} />
      </>
    );
  }

  return (
    <div className="flex items-center gap-2">
      <Select
        value={selectedPortfolioId ?? undefined}
        onChange={(e) => onSelect(e.target.value)}
        aria-label="Select portfolio"
        className="w-auto"
      >
        {portfolios.map((portfolio) => (
          <option key={portfolio.id} value={portfolio.id}>
            {portfolio.name}
            {portfolio.isDefault ? " (Default)" : ""}
          </option>
        ))}
      </Select>
      <Button size="sm" variant="outline" onClick={() => setDialogOpen(true)}>
        <Plus className="h-4 w-4" /> New
      </Button>
      <PortfolioFormDialog open={dialogOpen} onOpenChange={setDialogOpen} />
    </div>
  );
}
