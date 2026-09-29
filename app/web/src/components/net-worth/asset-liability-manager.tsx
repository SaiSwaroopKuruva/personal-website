"use client";

import { useState } from "react";
import { PiggyBank, Plus, Pencil, Trash2, Landmark } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { AssetFormDialog } from "@/components/net-worth/asset-form-dialog";
import { LiabilityFormDialog } from "@/components/net-worth/liability-form-dialog";
import { useDeleteAsset, useDeleteLiability, useNetWorthAssets, useNetWorthLiabilities } from "@/hooks/use-net-worth";
import { formatDate, formatInr } from "@/lib/utils";
import type { UserAsset } from "@/types/net-worth";
import type { UserLiability } from "@/types/net-worth";

export function AssetLiabilityManager() {
  const assetsQuery = useNetWorthAssets();
  const liabilitiesQuery = useNetWorthLiabilities();
  const deleteAsset = useDeleteAsset();
  const deleteLiability = useDeleteLiability();

  const [assetDialogOpen, setAssetDialogOpen] = useState(false);
  const [editingAsset, setEditingAsset] = useState<UserAsset | null>(null);
  const [liabilityDialogOpen, setLiabilityDialogOpen] = useState(false);
  const [editingLiability, setEditingLiability] = useState<UserLiability | null>(null);

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <CardTitle className="text-base">Manual Assets</CardTitle>
          <Button
            size="sm"
            onClick={() => {
              setEditingAsset(null);
              setAssetDialogOpen(true);
            }}
          >
            <Plus className="h-4 w-4" /> Add asset
          </Button>
        </CardHeader>
        <CardContent>
          {assetsQuery.isLoading ? (
            <Skeleton className="h-24 w-full" />
          ) : assetsQuery.data && assetsQuery.data.length > 0 ? (
            <div className="space-y-2">
              {assetsQuery.data.map((asset) => (
                <div key={asset.id} className="flex items-center justify-between rounded-lg border border-border p-3">
                  <div>
                    <p className="text-sm font-medium">{asset.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {asset.category.replaceAll("_", " ")} · as of {formatDate(asset.valuationDate)}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-semibold">{formatInr(asset.currentValue)}</span>
                    <Button
                      variant="ghost"
                      size="icon"
                      aria-label="Edit asset"
                      onClick={() => {
                        setEditingAsset(asset);
                        setAssetDialogOpen(true);
                      }}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      aria-label="Delete asset"
                      onClick={() => {
                        if (window.confirm("Remove this asset?")) deleteAsset.mutate(asset.id);
                      }}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState icon={PiggyBank} title="No manual assets" description="Add cash, fixed deposits, property, gold or other assets." />
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <CardTitle className="text-base">Manual Liabilities</CardTitle>
          <Button
            size="sm"
            onClick={() => {
              setEditingLiability(null);
              setLiabilityDialogOpen(true);
            }}
          >
            <Plus className="h-4 w-4" /> Add liability
          </Button>
        </CardHeader>
        <CardContent>
          {liabilitiesQuery.isLoading ? (
            <Skeleton className="h-24 w-full" />
          ) : liabilitiesQuery.data && liabilitiesQuery.data.length > 0 ? (
            <div className="space-y-2">
              {liabilitiesQuery.data.map((liability) => (
                <div key={liability.id} className="flex items-center justify-between rounded-lg border border-border p-3">
                  <div>
                    <p className="text-sm font-medium">{liability.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {liability.category.replaceAll("_", " ")} · as of {formatDate(liability.valuationDate)}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-semibold text-destructive">{formatInr(liability.currentValue)}</span>
                    <Button
                      variant="ghost"
                      size="icon"
                      aria-label="Edit liability"
                      onClick={() => {
                        setEditingLiability(liability);
                        setLiabilityDialogOpen(true);
                      }}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      aria-label="Delete liability"
                      onClick={() => {
                        if (window.confirm("Remove this liability?")) deleteLiability.mutate(liability.id);
                      }}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState icon={Landmark} title="No manual liabilities" description="Add loans, credit-card balances or other liabilities." />
          )}
        </CardContent>
      </Card>

      <AssetFormDialog open={assetDialogOpen} onOpenChange={setAssetDialogOpen} asset={editingAsset} />
      <LiabilityFormDialog open={liabilityDialogOpen} onOpenChange={setLiabilityDialogOpen} liability={editingLiability} />
    </div>
  );
}
