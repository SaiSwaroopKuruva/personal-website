"use client";

import { useState } from "react";
import { ArrowLeft, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ProgressBar } from "@/components/ui/progress-bar";
import { cn } from "@/lib/utils";
import type { RiskAnswer, RiskQuestion } from "@/types/risk";

interface QuestionnaireWizardProps {
  questions: RiskQuestion[];
  onComplete: (answers: RiskAnswer[]) => void;
  isSubmitting?: boolean;
}

export function QuestionnaireWizard({ questions, onComplete, isSubmitting }: QuestionnaireWizardProps) {
  const [stepIndex, setStepIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});

  const currentQuestion = questions[stepIndex];
  const progress = ((stepIndex + 1) / questions.length) * 100;
  const selectedOption = answers[currentQuestion?.code];
  const isLastStep = stepIndex === questions.length - 1;

  function selectOption(optionCode: string) {
    setAnswers((prev) => ({ ...prev, [currentQuestion.code]: optionCode }));
  }

  function goNext() {
    if (isLastStep) {
      const submission: RiskAnswer[] = questions.map((question) => ({
        questionCode: question.code,
        optionCode: answers[question.code],
      }));
      onComplete(submission);
      return;
    }
    setStepIndex((prev) => prev + 1);
  }

  function goBack() {
    setStepIndex((prev) => Math.max(0, prev - 1));
  }

  if (!currentQuestion) {
    return null;
  }

  return (
    <Card>
      <CardHeader className="space-y-3">
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>
            Question {stepIndex + 1} of {questions.length}
          </span>
          <span>{Math.round(progress)}%</span>
        </div>
        <ProgressBar value={progress} />
        <CardTitle>{currentQuestion.text}</CardTitle>
        {currentQuestion.helpText ? (
          <p className="text-sm text-muted-foreground">{currentQuestion.helpText}</p>
        ) : null}
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid gap-2">
          {currentQuestion.options.map((option) => (
            <button
              key={option.code}
              type="button"
              onClick={() => selectOption(option.code)}
              className={cn(
                "rounded-lg border px-4 py-3 text-left text-sm transition-colors hover:border-primary",
                selectedOption === option.code ? "border-primary bg-primary/5 font-medium" : "border-input"
              )}
            >
              {option.label}
            </button>
          ))}
        </div>

        <div className="flex justify-between pt-2">
          <Button variant="outline" onClick={goBack} disabled={stepIndex === 0}>
            <ArrowLeft className="h-4 w-4" /> Back
          </Button>
          <Button onClick={goNext} disabled={!selectedOption} isLoading={isLastStep && isSubmitting}>
            {isLastStep ? "Submit" : "Next"} <ArrowRight className="h-4 w-4" />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
