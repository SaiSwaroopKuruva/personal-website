import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Pagination } from "@/components/mutual-funds/pagination";

describe("Pagination", () => {
  it("renders nothing when there is only one page", () => {
    const { container } = render(<Pagination page={0} totalPages={1} onPageChange={() => {}} />);
    expect(container).toBeEmptyDOMElement();
  });

  it("disables Previous on the first page and Next on the last page", () => {
    render(<Pagination page={0} totalPages={3} onPageChange={() => {}} />);
    expect(screen.getByRole("button", { name: /previous/i })).toBeDisabled();
    expect(screen.getByRole("button", { name: /next/i })).not.toBeDisabled();
  });

  it("calls onPageChange with the next page number", async () => {
    let page = 0;
    const onPageChange = (p: number) => (page = p);
    render(<Pagination page={0} totalPages={3} onPageChange={onPageChange} />);

    await userEvent.click(screen.getByRole("button", { name: /next/i }));

    expect(page).toBe(1);
  });
});
