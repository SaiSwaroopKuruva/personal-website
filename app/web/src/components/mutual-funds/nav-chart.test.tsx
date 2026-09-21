import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { NavChart } from "@/components/mutual-funds/nav-chart";

describe("NavChart", () => {
  it("shows an empty state when there are no NAV points", () => {
    render(<NavChart points={[]} />);
    expect(screen.getByText("No NAV history available for this period")).toBeInTheDocument();
  });

  it("renders a chart with an accessible label when points are provided", () => {
    render(
      <NavChart
        points={[
          { date: "2026-01-01", nav: "10.0000" },
          { date: "2026-02-01", nav: "10.5000" },
        ]}
      />
    );
    expect(screen.getByRole("img", { name: /NAV chart/i })).toBeInTheDocument();
  });
});
