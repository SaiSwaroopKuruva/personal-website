"use client";

import { MailCheck } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useProfile } from "@/hooks/use-profile";
import { useResendVerification } from "@/hooks/use-security";

export default function ProfileVerifyEmailPage() {
  const { data: profile, isLoading } = useProfile();
  const resend = useResendVerification();

  if (isLoading || !profile) {
    return <Skeleton className="h-48" />;
  }

  return (
    <Card className="max-w-lg">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <MailCheck className="h-5 w-5 text-primary" /> Email Verification
        </CardTitle>
        <CardDescription>{profile.email}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <Badge variant={profile.emailVerified ? "success" : "outline"}>
          {profile.emailVerified ? "Verified" : "Not verified"}
        </Badge>
        {!profile.emailVerified ? (
          <div>
            <p className="text-sm text-muted-foreground">
              Check your inbox for a verification email, or request a new one below.
            </p>
            <Button className="mt-3" onClick={() => resend.mutate()} isLoading={resend.isPending}>
              Resend verification email
            </Button>
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">Your email address is verified.</p>
        )}
      </CardContent>
    </Card>
  );
}
