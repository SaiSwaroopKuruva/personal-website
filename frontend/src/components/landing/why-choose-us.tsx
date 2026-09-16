import { CheckCircle2 } from "lucide-react";

const REASONS = [
  "Fiduciary-first advice with no product-selling incentives",
  "Transparent, flat pricing with zero hidden charges",
  "Human advisors backed by data-driven recommendations",
  "Bank-grade encryption and secure authentication",
  "Award-winning customer support in your language",
];

export function WhyChooseUs() {
  return (
    <section id="why-us" className="bg-muted/30 py-24">
      <div className="container grid items-center gap-12 lg:grid-cols-2">
        <div>
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Why investors choose NiveshPath</h2>
          <p className="mt-4 text-muted-foreground">
            We built NiveshPath around one principle: your financial success comes first. Every recommendation is
            transparent, explainable, and aligned with your goals.
          </p>
          <ul className="mt-8 space-y-4">
            {REASONS.map((reason) => (
              <li key={reason} className="flex items-start gap-3">
                <CheckCircle2 className="mt-0.5 h-5 w-5 flex-shrink-0 text-primary" />
                <span className="text-sm">{reason}</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="grid grid-cols-2 gap-4">
          {[
            { value: "10L+", label: "Investors advised" },
            { value: "₹5,000Cr+", label: "Assets guided" },
            { value: "4.8/5", label: "Average rating" },
            { value: "24/7", label: "Support access" },
          ].map((stat) => (
            <div key={stat.label} className="rounded-xl border border-border bg-background p-6 text-center">
              <p className="text-2xl font-bold text-primary">{stat.value}</p>
              <p className="mt-1 text-xs text-muted-foreground">{stat.label}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
