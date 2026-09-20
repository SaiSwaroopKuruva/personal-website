"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { NotificationCard } from "@/components/profile/notification-card";
import { useNotificationPreferences, useUpdateNotificationPreferences } from "@/hooks/use-notifications";
import type { NotificationPreferences } from "@/types/notifications";

const NOTIFICATION_ITEMS: { key: keyof NotificationPreferences; title: string; description: string }[] = [
  { key: "email", title: "Email", description: "Receive updates via email" },
  { key: "sms", title: "SMS", description: "Receive updates via text message" },
  { key: "push", title: "Push notifications", description: "Receive updates on your devices" },
  { key: "inApp", title: "In-app notifications", description: "Show notifications within the app" },
  { key: "marketing", title: "Marketing", description: "Product updates, offers and promotions" },
  { key: "investmentAlerts", title: "Investment alerts", description: "Significant changes to your portfolio" },
  { key: "goalReminders", title: "Goal reminders", description: "Reminders about your investment goals" },
  { key: "marketUpdates", title: "Market updates", description: "Daily and weekly market movement summaries" },
  { key: "securityAlerts", title: "Security alerts", description: "Login attempts and account security events" },
  { key: "weeklyReports", title: "Weekly reports", description: "A weekly summary of your portfolio" },
  { key: "monthlyReports", title: "Monthly reports", description: "A monthly summary of your portfolio" },
];

export default function NotificationsPage() {
  const { data: preferences, isLoading } = useNotificationPreferences();
  const updatePreferences = useUpdateNotificationPreferences();

  if (isLoading || !preferences) {
    return <Skeleton className="h-96" />;
  }

  function handleToggle(key: keyof NotificationPreferences, value: boolean) {
    if (!preferences) return;
    updatePreferences.mutate({ ...preferences, [key]: value });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Notification Preferences</CardTitle>
        <CardDescription>Choose how you want to be notified</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {NOTIFICATION_ITEMS.map((item) => (
          <NotificationCard
            key={item.key}
            title={item.title}
            description={item.description}
            checked={preferences[item.key]}
            onCheckedChange={(value) => handleToggle(item.key, value)}
          />
        ))}
      </CardContent>
    </Card>
  );
}
