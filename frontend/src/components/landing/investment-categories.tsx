import { Building2, Coins, Home, LineChart, Shield, Sprout } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const CATEGORIES = [
  { icon: LineChart, title: "Mutual Funds", description: "Actively and passively managed funds across equity, debt, and hybrid categories." },
  { icon: Building2, title: "Equity", description: "Direct stock investing with research-backed recommendations." },
  { icon: Shield, title: "Insurance", description: "Life and health cover integrated into your overall financial plan." },
  { icon: Coins, title: "Fixed Income", description: "Bonds, FDs, and government schemes for stable, predictable returns." },
  { icon: Home, title: "Real Estate", description: "REITs and property-linked instruments for portfolio diversification." },
  { icon: Sprout, title: "Retirement", description: "NPS, PPF, and long-horizon planning for a secure retirement." },
];

export function InvestmentCategories() {
  return (
    <section id="investments" className="container py-24">
      <div className="mx-auto max-w-2xl text-center">
        <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Invest across every asset class</h2>
        <p className="mt-4 text-muted-foreground">
          Diversify your portfolio with access to the full range of Indian investment instruments.
        </p>
      </div>

      <div className="mt-16 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {CATEGORIES.map((category) => (
          <Card key={category.title} className="border-border/80 bg-gradient-to-br from-background to-muted/40">
            <CardHeader>
              <div className="mb-2 flex h-11 w-11 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <category.icon className="h-5 w-5" />
              </div>
              <CardTitle>{category.title}</CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">{category.description}</CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
