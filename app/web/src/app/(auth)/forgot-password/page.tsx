import Link from "next/link";
import { ForgotPasswordForm } from "@/components/auth/forgot-password-form";
import { ROUTES } from "@/lib/constants";

export default function ForgotPasswordPage() {
  return (
    <div>
      <div className="mb-6 text-center">
        <h1 className="text-2xl font-semibold">Reset your password</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Enter your email and we&apos;ll send you reset instructions
        </p>
      </div>
      <ForgotPasswordForm />
      <p className="mt-6 text-center text-sm text-muted-foreground">
        Remembered your password?{" "}
        <Link href={ROUTES.login} className="font-medium text-primary hover:underline">
          Log in
        </Link>
      </p>
    </div>
  );
}
