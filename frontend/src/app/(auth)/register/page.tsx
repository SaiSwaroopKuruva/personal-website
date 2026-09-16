import Link from "next/link";
import { RegisterForm } from "@/components/auth/register-form";
import { ROUTES } from "@/lib/constants";

export default function RegisterPage() {
  return (
    <div>
      <div className="mb-6 text-center">
        <h1 className="text-2xl font-semibold">Create your account</h1>
        <p className="mt-1 text-sm text-muted-foreground">Start your financial planning journey in minutes</p>
      </div>
      <RegisterForm />
      <p className="mt-6 text-center text-sm text-muted-foreground">
        Already have an account?{" "}
        <Link href={ROUTES.login} className="font-medium text-primary hover:underline">
          Log in
        </Link>
      </p>
    </div>
  );
}
