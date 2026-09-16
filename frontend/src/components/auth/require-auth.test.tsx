import { describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { RequireAuth } from "@/components/auth/require-auth";
import { useAuthStore } from "@/store/auth-store";

const replace = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace }),
}));

describe("RequireAuth", () => {
  it("redirects to login when the user is not authenticated", async () => {
    useAuthStore.setState({ accessToken: null, refreshToken: null, user: null, hasHydrated: true });

    render(
      <RequireAuth>
        <div>Protected content</div>
      </RequireAuth>
    );

    await waitFor(() => expect(replace).toHaveBeenCalledWith("/login"));
    expect(screen.queryByText("Protected content")).not.toBeInTheDocument();
  });

  it("renders children when the user is authenticated", () => {
    useAuthStore.setState({
      accessToken: "token",
      refreshToken: "refresh",
      user: {
        id: "1",
        firstName: "Asha",
        lastName: "Rao",
        email: "asha.rao@example.com",
        mobile: "9876543210",
        riskProfile: "MODERATE",
        createdAt: new Date().toISOString(),
      },
      hasHydrated: true,
    });

    render(
      <RequireAuth>
        <div>Protected content</div>
      </RequireAuth>
    );

    expect(screen.getByText("Protected content")).toBeInTheDocument();
  });
});
