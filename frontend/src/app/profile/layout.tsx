import { RequireAuth } from "@/components/auth/require-auth";
import { DashboardNav } from "@/components/dashboard/dashboard-nav";
import { ProfileNav } from "@/components/profile/profile-nav";

export default function ProfileLayout({ children }: { children: React.ReactNode }) {
  return (
    <RequireAuth>
      <div className="min-h-screen bg-muted/20">
        <DashboardNav />
        <main className="container space-y-6 py-8">
          <ProfileNav />
          {children}
        </main>
      </div>
    </RequireAuth>
  );
}
