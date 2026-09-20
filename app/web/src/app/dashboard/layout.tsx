import { RequireAuth } from "@/components/auth/require-auth";
import { DashboardNav } from "@/components/dashboard/dashboard-nav";

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  return (
    <RequireAuth>
      <div className="min-h-screen bg-muted/20">
        <DashboardNav />
        <main className="container py-8">{children}</main>
      </div>
    </RequireAuth>
  );
}
