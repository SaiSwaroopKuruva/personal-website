"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";

const TABS = [
  { href: ROUTES.profile, label: "Overview" },
  { href: ROUTES.profileEdit, label: "Edit Profile" },
  { href: ROUTES.profileAddress, label: "Address" },
  { href: ROUTES.profilePreferences, label: "Preferences" },
  { href: ROUTES.profileRisk, label: "Risk Profile" },
  { href: ROUTES.profileSecurity, label: "Security" },
  { href: ROUTES.profileDevices, label: "Devices" },
  { href: ROUTES.profileNotifications, label: "Notifications" },
  { href: ROUTES.profileChangePassword, label: "Change Password" },
];

export function ProfileNav() {
  const pathname = usePathname();

  return (
    <nav className="-mx-1 flex gap-1 overflow-x-auto border-b border-border pb-2">
      {TABS.map((tab) => (
        <Link
          key={tab.href}
          href={tab.href}
          className={cn(
            "shrink-0 rounded-md px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground",
            pathname === tab.href && "bg-primary text-primary-foreground hover:bg-primary/90 hover:text-primary-foreground"
          )}
        >
          {tab.label}
        </Link>
      ))}
    </nav>
  );
}
