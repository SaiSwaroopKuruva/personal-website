"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { authApi } from "@/lib/api/auth-api";
import { ROUTES } from "@/lib/constants";
import { useAuthStore } from "@/store/auth-store";
import type { LoginPayload, RegisterPayload } from "@/types/auth";

export function useLogin() {
  const router = useRouter();
  const setSession = useAuthStore((state) => state.setSession);

  return useMutation({
    mutationFn: (payload: LoginPayload) => authApi.login(payload),
    onSuccess: (data) => {
      setSession(data);
      toast.success(`Welcome back, ${data.user.firstName}!`);
      router.push(ROUTES.dashboard);
    },
    onError: () => {
      toast.error("Invalid email or password. Please try again.");
    },
  });
}

export function useRegister() {
  const router = useRouter();
  const setSession = useAuthStore((state) => state.setSession);

  return useMutation({
    mutationFn: (payload: RegisterPayload) => authApi.register(payload),
    onSuccess: (data) => {
      setSession(data);
      toast.success("Account created successfully!");
      router.push(ROUTES.dashboard);
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Could not create your account. Please try again.";
      toast.error(message);
    },
  });
}

export function useLogout() {
  const router = useRouter();
  const clearSession = useAuthStore((state) => state.clearSession);
  const refreshToken = useAuthStore((state) => state.refreshToken);

  return useMutation({
    mutationFn: () => (refreshToken ? authApi.logout(refreshToken) : Promise.resolve()),
    onSettled: () => {
      clearSession();
      router.push(ROUTES.login);
    },
  });
}

export function useCurrentUser() {
  const accessToken = useAuthStore((state) => state.accessToken);

  return useQuery({
    queryKey: ["auth", "me"],
    queryFn: () => authApi.me(),
    enabled: Boolean(accessToken),
    retry: false,
  });
}
