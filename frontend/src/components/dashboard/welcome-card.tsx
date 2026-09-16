"use client";

import { useAuthStore } from "@/store/auth-store";
import { Card, CardContent } from "@/components/ui/card";

export function WelcomeCard() {
  const user = useAuthStore((state) => state.user);
  const firstName = user?.firstName ?? "there";

  return (
    <Card className="bg-gradient-to-br from-primary to-primary/80 text-primary-foreground">
      <CardContent className="flex flex-col gap-1 pt-6">
        <p className="text-sm text-primary-foreground/80">Welcome back</p>
        <h2 className="text-2xl font-semibold">Hello, {firstName} 👋</h2>
        <p className="text-sm text-primary-foreground/80">
          Here&apos;s a snapshot of your financial journey today.
        </p>
      </CardContent>
    </Card>
  );
}
