import Link from "next/link";
import { LoginForm } from "@/components/auth/login-form";
import { ROUTES } from "@/lib/constants";

export default function LoginPage() {
  return (
    <div>
      <div className="mb-6 text-center">
        <h1 className="text-2xl font-semibold">Welcome back</h1>
        <p className="mt-1 text-sm text-muted-foreground">Log in to access your dashboard</p>
      </div>
      <LoginForm />
      <p className="mt-6 text-center text-sm text-muted-foreground">
        Don&apos;t have an account?{" "}
        <Link href={ROUTES.register} className="font-medium text-primary hover:underline">
          Create one
        </Link>
      </p>
    </div>
  );
}
