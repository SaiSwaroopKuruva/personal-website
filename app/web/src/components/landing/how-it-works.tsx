const STEPS = [
  {
    step: "01",
    title: "Create your profile",
    description: "Tell us about your goals, income, and risk appetite in a few guided steps.",
  },
  {
    step: "02",
    title: "Get a personalized plan",
    description: "Receive a curated investment strategy aligned to your risk profile and timelines.",
  },
  {
    step: "03",
    title: "Invest with confidence",
    description: "Track progress on your dashboard and adjust your plan as life changes.",
  },
];

export function HowItWorks() {
  return (
    <section id="how-it-works" className="bg-muted/30 py-24">
      <div className="container">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">How FinAdvisor works</h2>
          <p className="mt-4 text-muted-foreground">Three simple steps to a more confident financial future.</p>
        </div>

        <div className="mt-16 grid gap-8 md:grid-cols-3">
          {STEPS.map((item) => (
            <div key={item.step} className="relative rounded-xl border border-border bg-background p-6">
              <span className="text-4xl font-bold text-primary/20">{item.step}</span>
              <h3 className="mt-4 text-lg font-semibold">{item.title}</h3>
              <p className="mt-2 text-sm text-muted-foreground">{item.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
