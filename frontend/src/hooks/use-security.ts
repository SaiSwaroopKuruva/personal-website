"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { passwordApi, securityApi } from "@/lib/api/security-api";
import { emailVerificationApi } from "@/lib/api/security-api";

export function useDevices() {
  return useQuery({ queryKey: ["security", "devices"], queryFn: securityApi.getDevices });
}

export function useLoginHistory(page = 0, size = 20) {
  return useQuery({
    queryKey: ["security", "login-history", page, size],
    queryFn: () => securityApi.getLoginHistory(page, size),
  });
}

export function useLogoutAllDevices() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => securityApi.logoutAllDevices(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["security", "devices"] });
      toast.success("Logged out of all devices");
    },
  });
}

export function useRevokeDevice() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => securityApi.revokeDevice(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["security", "devices"] });
      toast.success("Device revoked");
    },
    onError: () => toast.error("Could not revoke this device. Please try again."),
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: ({ currentPassword, newPassword }: { currentPassword: string; newPassword: string }) =>
      passwordApi.changePassword(currentPassword, newPassword),
    onSuccess: () => toast.success("Password changed successfully"),
    onError: (error: unknown) => {
      const message =
        (error as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        "Could not change your password. Please try again.";
      toast.error(message);
    },
  });
}

export function useForgotPassword() {
  return useMutation({
    mutationFn: (email: string) => passwordApi.forgotPassword(email),
    onSuccess: () => toast.success("If that email exists, a reset link has been sent"),
  });
}

export function useResetPassword() {
  return useMutation({
    mutationFn: ({ token, newPassword }: { token: string; newPassword: string }) =>
      passwordApi.resetPassword(token, newPassword),
    onSuccess: () => toast.success("Password reset successfully. Please log in again."),
    onError: () => toast.error("This reset link is invalid or has expired."),
  });
}

export function useSendVerification() {
  return useMutation({
    mutationFn: () => emailVerificationApi.sendVerification(),
    onSuccess: () => toast.success("Verification email sent"),
  });
}

export function useVerifyEmail() {
  return useMutation({
    mutationFn: (token: string) => emailVerificationApi.verify(token),
    onSuccess: () => toast.success("Email verified successfully"),
    onError: () => toast.error("This verification link is invalid or has expired."),
  });
}

export function useResendVerification() {
  return useMutation({
    mutationFn: () => emailVerificationApi.resend(),
    onSuccess: () => toast.success("Verification email resent"),
  });
}
