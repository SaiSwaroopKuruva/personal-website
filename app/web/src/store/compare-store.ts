import { create } from "zustand";
import { persist } from "zustand/middleware";

const MAX_COMPARE_FUNDS = 4;

interface CompareState {
  schemeCodes: string[];
  add: (schemeCode: string) => void;
  remove: (schemeCode: string) => void;
  clear: () => void;
  isFull: () => boolean;
}

export const useCompareStore = create<CompareState>()(
  persist(
    (set, get) => ({
      schemeCodes: [],
      add: (schemeCode) =>
        set((state) => {
          if (state.schemeCodes.includes(schemeCode) || state.schemeCodes.length >= MAX_COMPARE_FUNDS) {
            return state;
          }
          return { schemeCodes: [...state.schemeCodes, schemeCode] };
        }),
      remove: (schemeCode) => set((state) => ({ schemeCodes: state.schemeCodes.filter((code) => code !== schemeCode) })),
      clear: () => set({ schemeCodes: [] }),
      isFull: () => get().schemeCodes.length >= MAX_COMPARE_FUNDS,
    }),
    { name: "fin-advisor-compare" }
  )
);

export { MAX_COMPARE_FUNDS };
