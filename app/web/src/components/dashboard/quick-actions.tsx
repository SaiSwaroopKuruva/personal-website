import { ArrowDownToLine, ArrowUpFromLine, PlusCircle, Target } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

const ACTIONS = [
  { icon: PlusCircle, label: "Invest" },
  { icon: ArrowUpFromLine, label: "Withdraw" },
  { icon: ArrowDownToLine, label: "Deposit" },
  { icon: Target, label: "Set Goal" },
];

export function QuickActions() {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Quick Actions</CardTitle>
      </CardHeader>
      <CardContent className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {ACTIONS.map((action) => (
          <Button key={action.label} variant="outline" className="flex h-auto flex-col gap-2 py-4">
            <action.icon className="h-5 w-5" />
            <span className="text-xs">{action.label}</span>
          </Button>
        ))}
      </CardContent>
    </Card>
  );
}
