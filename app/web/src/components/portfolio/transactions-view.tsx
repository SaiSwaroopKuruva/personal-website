"use client";

import { useState } from "react";
import { History, Pencil, Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/ui/empty-state";
import { Pagination } from "@/components/mutual-funds/pagination";
import { TransactionFormDialog } from "@/components/portfolio/transaction-form-dialog";
import { useDeleteTransaction, usePortfolioTransactions } from "@/hooks/use-portfolio";
import { formatDate, formatInr } from "@/lib/utils";
import type { AssetType, Transaction, TransactionType } from "@/types/portfolio";

export function TransactionsView({ portfolioId }: { portfolioId: string }) {
  const [assetType, setAssetType] = useState<AssetType | "">("");
  const [transactionType, setTransactionType] = useState<TransactionType | "">("");
  const [symbol, setSymbol] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [page, setPage] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState<Transaction | null>(null);

  const filters = {
    assetType: assetType || undefined,
    transactionType: transactionType || undefined,
    symbol: symbol || undefined,
    dateFrom: dateFrom || undefined,
    dateTo: dateTo || undefined,
    page,
    size: 20,
  };
  const transactionsQuery = usePortfolioTransactions(portfolioId, filters);
  const deleteTransaction = useDeleteTransaction(portfolioId);

  const openCreate = () => {
    setEditingTransaction(null);
    setFormOpen(true);
  };
  const openEdit = (tx: Transaction) => {
    setEditingTransaction(tx);
    setFormOpen(true);
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <Select value={assetType} onChange={(e) => { setAssetType(e.target.value as AssetType | ""); setPage(0); }} className="w-auto" aria-label="Filter by asset type">
            <option value="">All assets</option>
            <option value="STOCK">Stocks</option>
            <option value="MUTUAL_FUND">Mutual funds</option>
          </Select>
          <Select value={transactionType} onChange={(e) => { setTransactionType(e.target.value as TransactionType | ""); setPage(0); }} className="w-auto" aria-label="Filter by transaction type">
            <option value="">All types</option>
            {["BUY", "SELL", "PURCHASE", "REDEMPTION", "DIVIDEND", "FEE", "TAX", "ADJUSTMENT"].map((type) => (
              <option key={type} value={type}>{type}</option>
            ))}
          </Select>
          <Input placeholder="Symbol/scheme" value={symbol} onChange={(e) => { setSymbol(e.target.value); setPage(0); }} className="w-32" />
          <Input type="date" value={dateFrom} onChange={(e) => { setDateFrom(e.target.value); setPage(0); }} className="w-36" aria-label="From date" />
          <Input type="date" value={dateTo} onChange={(e) => { setDateTo(e.target.value); setPage(0); }} className="w-36" aria-label="To date" />
        </div>
        <Button size="sm" onClick={openCreate}>
          <Plus className="h-4 w-4" /> Add transaction
        </Button>
      </div>

      {transactionsQuery.data && transactionsQuery.data.content.length === 0 ? (
        <EmptyState icon={History} title="No transactions yet" description="Record your first buy, sell, purchase or redemption." />
      ) : (
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full text-sm">
            <thead className="bg-muted/40 text-left text-xs uppercase text-muted-foreground">
              <tr>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3">Asset</th>
                <th className="px-4 py-3">Type</th>
                <th className="px-4 py-3">Units</th>
                <th className="px-4 py-3">Gross</th>
                <th className="px-4 py-3">Net</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {transactionsQuery.data?.content.map((tx) => (
                <tr key={tx.id}>
                  <td className="px-4 py-3">{formatDate(tx.transactionDate)}</td>
                  <td className="px-4 py-3">
                    <p className="font-medium">{tx.assetType === "STOCK" ? tx.stockSymbol : tx.mutualFundSchemeCode}</p>
                    <p className="text-xs text-muted-foreground">{tx.assetType === "STOCK" ? tx.stockExchange : "AMFI"}</p>
                  </td>
                  <td className="px-4 py-3">
                    <Badge variant="outline">{tx.transactionType}</Badge>
                  </td>
                  <td className="px-4 py-3">{tx.quantity}</td>
                  <td className="px-4 py-3">{formatInr(tx.grossAmount)}</td>
                  <td className="px-4 py-3">{formatInr(tx.netAmount)}</td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-1">
                      <Button variant="ghost" size="icon" aria-label="Edit transaction" onClick={() => openEdit(tx)}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        aria-label="Delete transaction"
                        onClick={() => {
                          if (window.confirm("Delete this transaction? Holdings and gains will be recalculated.")) {
                            deleteTransaction.mutate(tx.id);
                          }
                        }}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {transactionsQuery.data ? (
        <Pagination page={page} totalPages={transactionsQuery.data.totalPages} onPageChange={setPage} />
      ) : null}

      <TransactionFormDialog portfolioId={portfolioId} open={formOpen} onOpenChange={setFormOpen} transaction={editingTransaction} />
    </div>
  );
}
