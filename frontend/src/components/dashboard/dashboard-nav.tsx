"use client";

import Link from "next/link";
import { LogOut, TrendingUp } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { useAuthStore } from "@/store/auth-store";
import { useLogout } from "@/hooks/use-auth";
import { ROUTES } from "@/lib/constants";

export function DashboardNav() {
  const user = useAuthStore((state) => state.user);
  const logout = useLogout();

  return (
    <header className="sticky top-0 z-40 border-b border-border/60 bg-background/80 backdrop-blur">
      <div className="container flex h-16 items-center justify-between">
        <Link href={ROUTES.dashboard} className="flex items-center gap-2 font-semibold">
          <TrendingUp className="h-6 w-6 text-primary" />
          <span>NiveshPath</span>
        </Link>

        <div className="flex items-center gap-3">
          <span className="hidden text-sm text-muted-foreground sm:inline">{user?.email}</span>
          <ThemeToggle />
          <Button variant="outline" size="sm" onClick={() => logout.mutate()} isLoading={logout.isPending}>
            <LogOut className="h-4 w-4" /> Log out
          </Button>
        </div>
      </div>
    </header>
  );
}
