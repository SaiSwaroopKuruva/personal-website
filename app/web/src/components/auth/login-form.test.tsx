import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { LoginForm } from "@/components/auth/login-form";

const mutate = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
}));

vi.mock("@/hooks/use-auth", () => ({
  useLogin: () => ({ mutate, isPending: false }),
}));

describe("LoginForm", () => {
  beforeEach(() => {
    mutate.mockClear();
  });

  it("shows validation errors for empty fields", async () => {
    render(<LoginForm />);

    await userEvent.click(screen.getByRole("button", { name: "Log in" }));

    expect(await screen.findByText("Email is required")).toBeInTheDocument();
    expect(await screen.findByText("Password is required")).toBeInTheDocument();
    expect(mutate).not.toHaveBeenCalled();
  });

  it("submits valid credentials", async () => {
    render(<LoginForm />);

    await userEvent.type(screen.getByLabelText("Email"), "investor@example.com");
    await userEvent.type(screen.getByLabelText("Password"), "StrongPass1");
    await userEvent.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() =>
      expect(mutate).toHaveBeenCalledWith({ email: "investor@example.com", password: "StrongPass1" })
    );
  });
});
