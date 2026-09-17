"use client";

import Link from "next/link";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ErrorState } from "@/components/ui/error-state";
import { ProfileCard } from "@/components/profile/profile-card";
import { RiskMeter } from "@/components/profile/risk-meter";
import { AllocationChart } from "@/components/profile/allocation-chart";
import { useProfile, useUploadPhoto } from "@/hooks/use-profile";
import { useLatestRiskAssessment } from "@/hooks/use-risk";
import { ROUTES } from "@/lib/constants";

export default function ProfileOverviewPage() {
  const { data: profile, isLoading, isError, refetch } = useProfile();
  const { data: riskAssessment } = useLatestRiskAssessment();
  const uploadPhoto = useUploadPhoto();

  if (isLoading) {
    return (
      <div className="grid gap-6 md:grid-cols-3">
        <Skeleton className="h-64 md:col-span-1" />
        <Skeleton className="h-64 md:col-span-2" />
      </div>
    );
  }

  if (isError || !profile) {
    return <ErrorState title="Could not load your profile" onRetry={() => refetch()} />;
  }

  function handlePhotoClick() {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = "image/png,image/jpeg,image/webp";
    input.onchange = () => {
      const file = input.files?.[0];
      if (file) {
        uploadPhoto.mutate(file);
      }
    };
    input.click();
  }

  return (
    <div className="grid gap-6 md:grid-cols-3">
      <div className="space-y-6 md:col-span-1">
        <ProfileCard profile={profile} onUploadPhoto={handlePhotoClick} />
        {uploadPhoto.isPending ? (
          <p className="flex items-center gap-2 text-xs text-muted-foreground">
            <Loader2 className="h-3 w-3 animate-spin" /> Uploading photo...
          </p>
        ) : null}
      </div>

      <div className="space-y-6 md:col-span-2">
        <Card>
          <CardHeader>
            <CardTitle>Investor Risk Profile</CardTitle>
          </CardHeader>
          <CardContent>
            {riskAssessment ? (
              <div className="grid gap-6 sm:grid-cols-2">
                <RiskMeter score={riskAssessment.score} riskLevel={riskAssessment.riskLevel} />
                <AllocationChart allocation={riskAssessment.recommendation.suggestedAllocation} />
              </div>
            ) : (
              <div className="flex flex-col items-center gap-3 py-6 text-center">
                <p className="text-sm text-muted-foreground">
                  You haven&apos;t completed your investor risk assessment yet.
                </p>
                <Button asChild>
                  <Link href={ROUTES.profileRisk}>Take the risk assessment</Link>
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            <Button asChild variant="outline" size="sm">
              <Link href={ROUTES.profileEdit}>Edit Profile</Link>
            </Button>
            <Button asChild variant="outline" size="sm">
              <Link href={ROUTES.profileAddress}>Manage Addresses</Link>
            </Button>
            <Button asChild variant="outline" size="sm">
              <Link href={ROUTES.profileSecurity}>Security Settings</Link>
            </Button>
            <Button asChild variant="outline" size="sm">
              <Link href={ROUTES.profileNotifications}>Notification Preferences</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
