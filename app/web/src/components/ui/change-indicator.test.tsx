import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { ChangeIndicator } from "@/components/ui/change-indicator";

describe("ChangeIndicator", () => {
  it("renders a positive change with a plus sign", () => {
    render(<ChangeIndicator changePercent="2.5" />);
    expect(screen.getByText("+2.50%")).toBeInTheDocument();
  });

  it("renders a negative change", () => {
    render(<ChangeIndicator changePercent="-1.2" />);
    expect(screen.getByText("-1.20%")).toBeInTheDocument();
  });

  it("renders a neutral dash when the value is missing", () => {
    render(<ChangeIndicator changePercent={null} />);
    expect(screen.getByText("—")).toBeInTheDocument();
  });
});
