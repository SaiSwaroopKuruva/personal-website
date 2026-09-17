import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QuestionnaireWizard } from "@/components/profile/questionnaire-wizard";
import type { RiskQuestion } from "@/types/risk";

const QUESTIONS: RiskQuestion[] = [
  {
    code: "AGE",
    text: "What is your age group?",
    helpText: null,
    options: [
      { code: "UNDER_30", label: "Under 30", score: 100 },
      { code: "ABOVE_60", label: "Above 60", score: 0 },
    ],
  },
  {
    code: "RISK_APPETITE",
    text: "What is your risk appetite?",
    helpText: null,
    options: [
      { code: "VERY_HIGH", label: "Very high", score: 100 },
      { code: "VERY_LOW", label: "Very low", score: 0 },
    ],
  },
];

describe("QuestionnaireWizard", () => {
  it("walks through each question and submits collected answers", async () => {
    const onComplete = vi.fn();
    render(<QuestionnaireWizard questions={QUESTIONS} onComplete={onComplete} />);

    expect(screen.getByText("What is your age group?")).toBeInTheDocument();

    await userEvent.click(screen.getByRole("button", { name: "Under 30" }));
    await userEvent.click(screen.getByRole("button", { name: "Next" }));

    expect(screen.getByText("What is your risk appetite?")).toBeInTheDocument();

    await userEvent.click(screen.getByRole("button", { name: "Very high" }));
    await userEvent.click(screen.getByRole("button", { name: "Submit" }));

    expect(onComplete).toHaveBeenCalledWith([
      { questionCode: "AGE", optionCode: "UNDER_30" },
      { questionCode: "RISK_APPETITE", optionCode: "VERY_HIGH" },
    ]);
  });

  it("disables next until an option is selected", () => {
    render(<QuestionnaireWizard questions={QUESTIONS} onComplete={vi.fn()} />);
    expect(screen.getByRole("button", { name: /Next/ })).toBeDisabled();
  });
});
