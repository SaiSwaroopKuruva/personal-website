import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { FundCard } from "@/components/mutual-funds/fund-card";
import { useAuthStore } from "@/store/auth-store";
import { useCompareStore } from "@/store/compare-store";
import type { MutualFundSummary } from "@/types/mutual-fund";

const addFavoriteMutate = vi.fn();
const removeFavoriteMutate = vi.fn();

vi.mock("@/hooks/use-mutual-funds", () => ({
  useToggleFavorite: () => ({
    add: { mutate: addFavoriteMutate },
    remove: { mutate: removeFavoriteMutate },
  }),
}));

const fund: MutualFundSummary = {
  schemeCode: "DEMO001",
  isin: "INE000A01011",
  schemeName: "Nilgiri Bluechip Equity Fund",
  shortName: "Nilgiri Bluechip",
  amcCode: "NILGIRI",
  amcName: "Nilgiri Mutual Fund",
  category: "Equity",
  subCategory: "Large Cap",
  planType: "DIRECT",
  optionType: "GROWTH",
  riskLevel: "VERY_HIGH",
  nav: "14.5678",
  navDate: "2026-09-19",
  expenseRatio: "1.05",
  aum: "12450.75",
  minimumSip: "500",
  minimumLumpsum: "5000",
  oneYearReturn: "18.50",
  threeYearReturn: "15.20",
  fiveYearReturn: null,
  favorite: false,
};

describe("FundCard", () => {
  beforeEach(() => {
    addFavoriteMutate.mockClear();
    removeFavoriteMutate.mockClear();
    useAuthStore.setState({ accessToken: null, refreshToken: null, user: null, hasHydrated: true });
    useCompareStore.setState({ schemeCodes: [] });
  });

  it("renders fund name, AMC, category and key metrics", () => {
    render(<FundCard fund={fund} />);

    expect(screen.getByText("Nilgiri Bluechip Equity Fund")).toBeInTheDocument();
    expect(screen.getByText("Nilgiri Mutual Fund")).toBeInTheDocument();
    expect(screen.getByText("Equity")).toBeInTheDocument();
    expect(screen.getByText("₹14.5678")).toBeInTheDocument();
  });

  it("prompts login when an unauthenticated user tries to favorite a fund", async () => {
    render(<FundCard fund={fund} />);

    await userEvent.click(screen.getByRole("button", { name: /add to favorites/i }));

    expect(addFavoriteMutate).not.toHaveBeenCalled();
  });

  it("calls addFavorite when an authenticated user favorites a fund", async () => {
    useAuthStore.setState({ accessToken: "token", refreshToken: "refresh", user: null, hasHydrated: true });
    render(<FundCard fund={fund} />);

    await userEvent.click(screen.getByRole("button", { name: /add to favorites/i }));

    expect(addFavoriteMutate).toHaveBeenCalledWith("DEMO001");
  });

  it("adds the fund to the compare list", async () => {
    render(<FundCard fund={fund} />);

    await userEvent.click(screen.getByRole("button", { name: /compare/i }));

    expect(useCompareStore.getState().schemeCodes).toContain("DEMO001");
  });
});
