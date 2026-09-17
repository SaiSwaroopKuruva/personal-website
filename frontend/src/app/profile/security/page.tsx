"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { SecurityTimeline } from "@/components/profile/security-timeline";
import { useLoginHistory, useLogoutAllDevices } from "@/hooks/use-security";
import { History, LogOut } from "lucide-react";

export default function SecurityPage() {
  const { data: history, isLoading } = useLoginHistory();
  const logoutAll = useLogoutAllDevices();

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle>Account Security</CardTitle>
            <CardDescription>Review recent activity and manage active sessions</CardDescription>
          </div>
          <Button variant="outline" onClick={() => logoutAll.mutate()} isLoading={logoutAll.isPending}>
            <LogOut className="h-4 w-4" /> Log out of all devices
          </Button>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-48" />
          ) : !history || history.content.length === 0 ? (
            <EmptyState icon={History} title="No recent activity" description="Sign-in and security events will appear here." />
          ) : (
            <SecurityTimeline entries={history.content} />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
