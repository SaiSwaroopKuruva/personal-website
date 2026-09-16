import { ArrowDownLeft, ArrowUpRight } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

// Placeholder data — real transaction history is planned for a later phase.
const ACTIVITIES = [
  { type: "credit", label: "SIP - Nifty Index Fund", amount: "₹5,000", date: "Today" },
  { type: "debit", label: "Withdrawal to bank", amount: "₹2,000", date: "Yesterday" },
  { type: "credit", label: "SIP - ELSS Tax Saver", amount: "₹3,000", date: "3 days ago" },
];

export function RecentActivity() {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Recent Activity</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {ACTIVITIES.map((activity) => (
          <div key={activity.label} className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div
                className={`flex h-9 w-9 items-center justify-center rounded-full ${
                  activity.type === "credit"
                    ? "bg-emerald-100 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400"
                    : "bg-red-100 text-red-600 dark:bg-red-500/10 dark:text-red-400"
                }`}
              >
                {activity.type === "credit" ? <ArrowDownLeft className="h-4 w-4" /> : <ArrowUpRight className="h-4 w-4" />}
              </div>
              <div>
                <p className="text-sm font-medium">{activity.label}</p>
                <p className="text-xs text-muted-foreground">{activity.date}</p>
              </div>
            </div>
            <p className="text-sm font-semibold">{activity.amount}</p>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
