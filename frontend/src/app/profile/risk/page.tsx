"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ErrorState } from "@/components/ui/error-state";
import { AllocationChart } from "@/components/profile/allocation-chart";
import { QuestionnaireWizard } from "@/components/profile/questionnaire-wizard";
import { RiskMeter } from "@/components/profile/risk-meter";
import { useLatestRiskAssessment, useRiskQuestions, useSubmitRiskAssessment } from "@/hooks/use-risk";

export default function RiskProfilePage() {
  const { data: questions, isLoading: questionsLoading } = useRiskQuestions();
  const { data: latest, isLoading: latestLoading } = useLatestRiskAssessment();
  const submitAssessment = useSubmitRiskAssessment();
  const [retaking, setRetaking] = useState(false);

  if (questionsLoading || latestLoading) {
    return <Skeleton className="h-96" />;
  }

  if (!retaking && latest) {
    return (
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Your Investor Risk Profile</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-6 sm:grid-cols-2">
              <RiskMeter score={latest.score} riskLevel={latest.riskLevel} />
              <AllocationChart allocation={latest.recommendation.suggestedAllocation} />
            </div>
            <p className="text-sm text-muted-foreground">{latest.recommendation.summary}</p>
            <div>
              <p className="text-sm font-medium">Suggested investment horizon</p>
              <p className="text-sm text-muted-foreground">{latest.recommendation.suggestedInvestmentHorizon}</p>
            </div>
            <div>
              <p className="text-sm font-medium">Suggested fund categories</p>
              <p className="text-sm text-muted-foreground">
                {latest.recommendation.suggestedFundCategories.join(", ")}
              </p>
            </div>
            <Button variant="outline" onClick={() => setRetaking(true)}>
              Retake assessment
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!questions) {
    return <ErrorState title="Could not load the risk questionnaire" />;
  }

  return (
    <QuestionnaireWizard
      questions={questions}
      isSubmitting={submitAssessment.isPending}
      onComplete={(answers) => submitAssessment.mutate(answers, { onSuccess: () => setRetaking(false) })}
    />
  );
}
