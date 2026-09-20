import { describe, expect, it } from "vitest";
import { changePasswordSchema } from "@/lib/validations/password";

describe("changePasswordSchema", () => {
  it("accepts a strong matching password pair", () => {
    const result = changePasswordSchema.safeParse({
      currentPassword: "OldPassword123!",
      newPassword: "NewStrongPass123!",
      confirmPassword: "NewStrongPass123!",
    });

    expect(result.success).toBe(true);
  });

  it("rejects passwords shorter than 12 characters", () => {
    const result = changePasswordSchema.safeParse({
      currentPassword: "OldPassword123!",
      newPassword: "Short1!",
      confirmPassword: "Short1!",
    });

    expect(result.success).toBe(false);
  });

  it("rejects when confirmation does not match", () => {
    const result = changePasswordSchema.safeParse({
      currentPassword: "OldPassword123!",
      newPassword: "NewStrongPass123!",
      confirmPassword: "Mismatch123!",
    });

    expect(result.success).toBe(false);
  });
});
