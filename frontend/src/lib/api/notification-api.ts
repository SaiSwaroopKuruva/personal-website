import { apiClient } from "@/lib/api/axios-client";
import type { NotificationPreferences } from "@/types/notifications";

export const notificationApi = {
  getPreferences: () =>
    apiClient.get<NotificationPreferences>("/api/notifications/preferences").then((res) => res.data),

  updatePreferences: (preferences: NotificationPreferences) =>
    apiClient.put<NotificationPreferences>("/api/notifications/preferences", preferences).then((res) => res.data),
};
