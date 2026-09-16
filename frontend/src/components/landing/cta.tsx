import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ROUTES } from "@/lib/constants";

export function Cta() {
  return (
    <section className="container py-24">
      <div className="relative overflow-hidden rounded-2xl bg-primary px-8 py-16 text-center text-primary-foreground">
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_bottom,_rgba(255,255,255,0.15),_transparent_60%)]"
        />
        <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Start your investing journey today</h2>
        <p className="mx-auto mt-4 max-w-xl text-primary-foreground/90">
          Join thousands of Indians building wealth with a personalized, transparent financial plan.
        </p>
        <Button size="lg" variant="secondary" className="mt-8" asChild>
          <Link href={ROUTES.register}>
            Create your free account <ArrowRight className="h-4 w-4" />
          </Link>
        </Button>
      </div>
    </section>
  );
}
