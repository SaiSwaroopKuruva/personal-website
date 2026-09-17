import Link from "next/link";
import { TrendingUp } from "lucide-react";
import { ROUTES } from "@/lib/constants";

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-muted/30 px-4 py-12">
      <Link href={ROUTES.home} className="mb-8 flex items-center gap-2 text-lg font-semibold">
        <TrendingUp className="h-6 w-6 text-primary" />
        FinAdvisor
      </Link>
      <div className="w-full max-w-md rounded-2xl border border-border bg-background p-8 shadow-sm">
        {children}
      </div>
    </div>
  );
}
