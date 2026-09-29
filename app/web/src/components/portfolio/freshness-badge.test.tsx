import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { FreshnessBadge } from "@/components/portfolio/freshness-badge";

describe("FreshnessBadge", () => {
  it("labels a current valuation clearly", () => {
    render(<FreshnessBadge status="CURRENT" />);
    expect(screen.getByText("Current")).toBeInTheDocument();
  });

  it("labels a stale valuation clearly", () => {
    render(<FreshnessBadge status="STALE" />);
    expect(screen.getByText("Stale")).toBeInTheDocument();
  });

  it("labels an unavailable valuation clearly", () => {
    render(<FreshnessBadge status="UNAVAILABLE" />);
    expect(screen.getByText("Unavailable")).toBeInTheDocument();
  });
});
