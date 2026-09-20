"use client";

import { useEffect, useRef } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Loader2, MailCheck, XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ROUTES } from "@/lib/constants";
import { useVerifyEmail } from "@/hooks/use-security";

export default function VerifyEmailPage() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token");
  const verifyEmail = useVerifyEmail();
  const attemptedRef = useRef(false);

  useEffect(() => {
    if (token && !attemptedRef.current) {
      attemptedRef.current = true;
      verifyEmail.mutate(token);
    }
  }, [token, verifyEmail]);

  if (token) {
    if (verifyEmail.isPending) {
      return (
        <div className="flex flex-col items-center gap-4 text-center">
          <Loader2 className="h-10 w-10 animate-spin text-primary" />
          <p className="text-sm text-muted-foreground">Verifying your email address...</p>
        </div>
      );
    }

    if (verifyEmail.isSuccess) {
      return (
        <div className="flex flex-col items-center gap-4 text-center">
          <MailCheck className="h-12 w-12 text-primary" />
          <h1 className="text-2xl font-semibold">Email verified</h1>
          <p className="text-sm text-muted-foreground">Your email address has been successfully verified.</p>
          <Button asChild className="mt-2 w-full">
            <Link href={ROUTES.dashboard}>Go to dashboard</Link>
          </Button>
        </div>
      );
    }

    if (verifyEmail.isError) {
      return (
        <div className="flex flex-col items-center gap-4 text-center">
          <XCircle className="h-12 w-12 text-destructive" />
          <h1 className="text-2xl font-semibold">Verification failed</h1>
          <p className="text-sm text-muted-foreground">
            This verification link is invalid or has expired. Request a new one from your profile.
          </p>
          <Button asChild className="mt-2 w-full">
            <Link href={ROUTES.login}>Back to login</Link>
          </Button>
        </div>
      );
    }
  }

  return (
    <div className="flex flex-col items-center gap-4 text-center">
      <MailCheck className="h-12 w-12 text-primary" />
      <h1 className="text-2xl font-semibold">Verify your email</h1>
      <p className="text-sm text-muted-foreground">
        We&apos;ve sent a verification link to your email address. Click the link to activate your account.
      </p>
      <Button asChild className="mt-2 w-full">
        <Link href={ROUTES.login}>Back to login</Link>
      </Button>
    </div>
  );
}
