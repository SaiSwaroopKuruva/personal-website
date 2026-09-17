"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { notificationApi } from "@/lib/api/notification-api";
import type { NotificationPreferences } from "@/types/notifications";

const NOTIFICATIONS_KEY = ["notifications", "preferences"];

export function useNotificationPreferences() {
  return useQuery({ queryKey: NOTIFICATIONS_KEY, queryFn: notificationApi.getPreferences });
}

export function useUpdateNotificationPreferences() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (preferences: NotificationPreferences) => notificationApi.updatePreferences(preferences),
    onSuccess: (data) => {
      queryClient.setQueryData(NOTIFICATIONS_KEY, data);
      toast.success("Notification preferences saved");
    },
    onError: () => toast.error("Could not save notification preferences. Please try again."),
  });
}
