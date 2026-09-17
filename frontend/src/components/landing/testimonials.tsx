import { Star } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

const TESTIMONIALS = [
  {
    name: "Priya Sharma",
    role: "Software Engineer, Bengaluru",
    quote:
      "FinAdvisor helped me build a clear roadmap for my retirement and my daughter's education — all in one dashboard.",
  },
  {
    name: "Rohan Mehta",
    role: "Business Owner, Pune",
    quote:
      "The goal-based planning tools made it simple to understand where my money should go and why.",
  },
  {
    name: "Ananya Iyer",
    role: "Doctor, Chennai",
    quote: "Transparent pricing and genuinely helpful advisors — a refreshing change from traditional advisory firms.",
  },
];

export function Testimonials() {
  return (
    <section id="testimonials" className="container py-24">
      <div className="mx-auto max-w-2xl text-center">
        <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Trusted by investors across India</h2>
      </div>

      <div className="mt-16 grid gap-6 md:grid-cols-3">
        {TESTIMONIALS.map((testimonial) => (
          <Card key={testimonial.name}>
            <CardContent className="pt-6">
              <div className="flex gap-1 text-amber-500">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Star key={index} className="h-4 w-4 fill-current" />
                ))}
              </div>
              <p className="mt-4 text-sm text-muted-foreground">&ldquo;{testimonial.quote}&rdquo;</p>
              <div className="mt-6">
                <p className="text-sm font-semibold">{testimonial.name}</p>
                <p className="text-xs text-muted-foreground">{testimonial.role}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
