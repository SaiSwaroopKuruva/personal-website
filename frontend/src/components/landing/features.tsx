import { BarChart3, PiggyBank, ShieldCheck, Sparkles, Target, Wallet } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const FEATURES = [
  {
    icon: Target,
    title: "Goal-based planning",
    description: "Map every rupee to a goal — retirement, education, or a dream home — with clear timelines.",
  },
  {
    icon: BarChart3,
    title: "Portfolio insights",
    description: "Real-time visibility into performance, asset allocation, and risk exposure across your holdings.",
  },
  {
    icon: ShieldCheck,
    title: "Bank-grade security",
    description: "Your data and investments are protected with encryption and industry-standard safeguards.",
  },
  {
    icon: PiggyBank,
    title: "Tax-smart investing",
    description: "Recommendations that account for India's tax regime, so you keep more of what you earn.",
  },
  {
    icon: Wallet,
    title: "Low-cost access",
    description: "Transparent pricing with no hidden charges — know exactly what you pay for.",
  },
  {
    icon: Sparkles,
    title: "Personalized guidance",
    description: "Advice tailored to your risk profile, income stage, and long-term financial ambitions.",
  },
];

export function Features() {
  return (
    <section id="features" className="container py-24">
      <div className="mx-auto max-w-2xl text-center">
        <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Everything you need to grow your wealth</h2>
        <p className="mt-4 text-muted-foreground">
          A complete toolkit for planning, investing, and tracking your financial future — all in one place.
        </p>
      </div>

      <div className="mt-16 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {FEATURES.map((feature) => (
          <Card key={feature.title} className="transition-shadow hover:shadow-md">
            <CardHeader>
              <div className="mb-2 flex h-11 w-11 items-center justify-center rounded-lg bg-accent text-accent-foreground">
                <feature.icon className="h-5 w-5" />
              </div>
              <CardTitle>{feature.title}</CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">{feature.description}</CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
