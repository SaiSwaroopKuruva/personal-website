import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { NotificationCard } from "@/components/profile/notification-card";

describe("NotificationCard", () => {
  it("renders the title, description and current state", () => {
    render(
      <NotificationCard title="Email" description="Receive updates via email" checked onCheckedChange={vi.fn()} />
    );

    expect(screen.getByText("Email")).toBeInTheDocument();
    expect(screen.getByText("Receive updates via email")).toBeInTheDocument();
    expect(screen.getByRole("switch")).toHaveAttribute("aria-checked", "true");
  });

  it("calls onCheckedChange with the toggled value", async () => {
    const onCheckedChange = vi.fn();
    render(<NotificationCard title="SMS" description="Text messages" checked={false} onCheckedChange={onCheckedChange} />);

    await userEvent.click(screen.getByRole("switch"));

    expect(onCheckedChange).toHaveBeenCalledWith(true);
  });
});
