"use client";

import { useState } from "react";
import { ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";

const FAQS = [
  {
    question: "Is FinAdvisor regulated?",
    answer:
      "FinAdvisor follows SEBI-aligned advisory practices and partners with registered investment advisors to guide recommendations.",
  },
  {
    question: "How much does it cost to get started?",
    answer: "Creating an account and building your financial plan is free. Transparent pricing applies only when you choose to invest.",
  },
  {
    question: "Is my data secure?",
    answer: "Yes. We use industry-standard encryption, secure authentication, and never share your data without consent.",
  },
  {
    question: "Can I change my risk profile later?",
    answer: "Absolutely — your risk profile and goals can be updated anytime from your dashboard as your circumstances change.",
  },
];

export function Faq() {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  return (
    <section id="faq" className="bg-muted/30 py-24">
      <div className="container max-w-3xl">
        <h2 className="text-center text-3xl font-bold tracking-tight sm:text-4xl">Frequently asked questions</h2>

        <div className="mt-12 space-y-3">
          {FAQS.map((faq, index) => {
            const isOpen = openIndex === index;
            return (
              <div key={faq.question} className="rounded-xl border border-border bg-background">
                <button
                  className="flex w-full items-center justify-between px-5 py-4 text-left text-sm font-medium"
                  aria-expanded={isOpen}
                  onClick={() => setOpenIndex(isOpen ? null : index)}
                >
                  {faq.question}
                  <ChevronDown className={cn("h-4 w-4 shrink-0 transition-transform", isOpen && "rotate-180")} />
                </button>
                {isOpen && <p className="px-5 pb-4 text-sm text-muted-foreground">{faq.answer}</p>}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
