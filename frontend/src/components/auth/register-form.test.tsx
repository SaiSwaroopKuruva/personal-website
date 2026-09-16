import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { RegisterForm } from "@/components/auth/register-form";

const mutate = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
}));

vi.mock("@/hooks/use-auth", () => ({
  useRegister: () => ({ mutate, isPending: false }),
}));

describe("RegisterForm", () => {
  beforeEach(() => {
    mutate.mockClear();
  });

  it("shows an error when passwords do not match", async () => {
    render(<RegisterForm />);

    await userEvent.type(screen.getByLabelText("First name"), "Asha");
    await userEvent.type(screen.getByLabelText("Last name"), "Rao");
    await userEvent.type(screen.getByLabelText("Email"), "asha.rao@example.com");
    await userEvent.type(screen.getByLabelText("Mobile number"), "9876543210");
    await userEvent.type(screen.getByLabelText("Password"), "StrongPass1");
    await userEvent.type(screen.getByLabelText("Confirm password"), "DifferentPass1");
    await userEvent.click(screen.getByRole("button", { name: "Create account" }));

    expect(await screen.findByText("Passwords do not match")).toBeInTheDocument();
    expect(mutate).not.toHaveBeenCalled();
  });
});
