import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { Disclaimer } from "@/components/mutual-funds/disclaimer";

describe("Disclaimer", () => {
  it("renders the default investment risk disclaimer", () => {
    render(<Disclaimer />);
    expect(screen.getByRole("note", { name: "Investment risk disclaimer" })).toHaveTextContent(/market risks/i);
  });

  it("renders custom disclaimer text when provided", () => {
    render(<Disclaimer text="Custom disclaimer text" />);
    expect(screen.getByText("Custom disclaimer text")).toBeInTheDocument();
  });
});
