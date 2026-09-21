import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SipCalculatorPage from "@/app/calculators/sip/page";

const mutate = vi.fn();

vi.mock("@/hooks/use-calculators", () => ({
  useSipCalculator: () => ({ mutate, isPending: false, data: undefined, reset: vi.fn() }),
}));

describe("SipCalculatorPage", () => {
  beforeEach(() => mutate.mockClear());

  it("submits the default values", async () => {
    render(<SipCalculatorPage />);

    await userEvent.click(screen.getByRole("button", { name: "Calculate" }));

    expect(mutate).toHaveBeenCalledWith({ monthlyInvestment: 10000, expectedAnnualReturn: 12, durationYears: 10 });
  });

  it("shows a validation error for an out-of-range return", async () => {
    render(<SipCalculatorPage />);

    const returnInput = screen.getByLabelText("Expected Annual Return (%)");
    await userEvent.clear(returnInput);
    await userEvent.type(returnInput, "45");
    await userEvent.click(screen.getByRole("button", { name: "Calculate" }));

    expect(await screen.findByText(/must be 30% or less/i)).toBeInTheDocument();
    expect(mutate).not.toHaveBeenCalled();
  });
});
