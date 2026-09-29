import { create } from "zustand";
import { persist } from "zustand/middleware";

const MAX_COMPARE_STOCKS = 4;

interface StockCompareState {
  symbols: string[];
  add: (symbol: string) => void;
  remove: (symbol: string) => void;
  clear: () => void;
  isFull: () => boolean;
}

export const useStockCompareStore = create<StockCompareState>()(
  persist(
    (set, get) => ({
      symbols: [],
      add: (symbol) =>
        set((state) => {
          if (state.symbols.includes(symbol) || state.symbols.length >= MAX_COMPARE_STOCKS) {
            return state;
          }
          return { symbols: [...state.symbols, symbol] };
        }),
      remove: (symbol) => set((state) => ({ symbols: state.symbols.filter((s) => s !== symbol) })),
      clear: () => set({ symbols: [] }),
      isFull: () => get().symbols.length >= MAX_COMPARE_STOCKS,
    }),
    { name: "fin-advisor-stock-compare" }
  )
);

export { MAX_COMPARE_STOCKS };
