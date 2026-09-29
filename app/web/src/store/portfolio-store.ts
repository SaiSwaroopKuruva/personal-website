import { create } from "zustand";

interface PortfolioSelectionState {
  selectedPortfolioId: string | null;
  setSelectedPortfolioId: (id: string | null) => void;
}

/** In-memory only (not persisted) - the portfolio pages default to the user's default/first portfolio on load. */
export const usePortfolioStore = create<PortfolioSelectionState>()((set) => ({
  selectedPortfolioId: null,
  setSelectedPortfolioId: (id) => set({ selectedPortfolioId: id }),
}));
