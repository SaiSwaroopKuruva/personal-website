import Link from "next/link";
import { MailCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ROUTES } from "@/lib/constants";

// Phase 1: placeholder UI — email delivery/verification infrastructure is not implemented yet.
export default function VerifyEmailPage() {
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
