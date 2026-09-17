import Link from "next/link";
import { ArrowRight, ShieldCheck, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ROUTES } from "@/lib/constants";

export function Hero() {
  return (
    <section className="relative overflow-hidden">
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(circle_at_top,_hsl(var(--primary)/0.15),_transparent_60%)]"
      />
      <div className="container flex flex-col items-center gap-8 py-24 text-center md:py-32">
        <Badge variant="secondary" className="animate-fade-in gap-1.5">
          <Sparkles className="h-3.5 w-3.5" /> Built for Indian investors
        </Badge>

        <h1 className="max-w-3xl animate-fade-in text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl">
          Smarter financial advice for a wealthier tomorrow
        </h1>

        <p className="max-w-2xl animate-fade-in text-balance text-lg text-muted-foreground">
          FinAdvisor brings personalized investment planning, goal tracking, and portfolio insights to every Indian
          investor — backed by transparent, data-driven guidance.
        </p>

        <div className="flex animate-fade-in flex-col gap-3 sm:flex-row">
          <Button size="lg" asChild>
            <Link href={ROUTES.register}>
              Start investing free <ArrowRight className="h-4 w-4" />
            </Link>
          </Button>
          <Button size="lg" variant="outline" asChild>
            <Link href="#how-it-works">See how it works</Link>
          </Button>
        </div>

        <div className="flex animate-fade-in items-center gap-2 text-sm text-muted-foreground">
          <ShieldCheck className="h-4 w-4 text-primary" />
          SEBI-aligned practices &middot; Bank-grade security &middot; No hidden fees
        </div>
      </div>
    </section>
  );
}
