import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

// Placeholder data — real goal tracking is planned for a later phase.
const GOALS = [
  { name: "Retirement Fund", progress: 62 },
  { name: "Child's Education", progress: 34 },
  { name: "Emergency Fund", progress: 88 },
];

export function InvestmentGoals() {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Investment Goals</CardTitle>
      </CardHeader>
      <CardContent className="space-y-5">
        {GOALS.map((goal) => (
          <div key={goal.name}>
            <div className="mb-1.5 flex items-center justify-between text-sm">
              <span className="font-medium">{goal.name}</span>
              <span className="text-muted-foreground">{goal.progress}%</span>
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-muted">
              <div className="h-full rounded-full bg-primary" style={{ width: `${goal.progress}%` }} />
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
