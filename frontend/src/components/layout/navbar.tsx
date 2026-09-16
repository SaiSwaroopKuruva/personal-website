"use client";

import Link from "next/link";
import { useState } from "react";
import { Menu, TrendingUp, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { ROUTES } from "@/lib/constants";

const NAV_LINKS = [
  { label: "Features", href: "#features" },
  { label: "How It Works", href: "#how-it-works" },
  { label: "Investments", href: "#investments" },
  { label: "Testimonials", href: "#testimonials" },
  { label: "FAQ", href: "#faq" },
];

export function Navbar() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border/60 bg-background/80 backdrop-blur">
      <div className="container flex h-16 items-center justify-between">
        <Link href={ROUTES.home} className="flex items-center gap-2 font-semibold">
          <TrendingUp className="h-6 w-6 text-primary" />
          <span>NiveshPath</span>
        </Link>

        <nav className="hidden items-center gap-6 md:flex">
          {NAV_LINKS.map((link) => (
            <a key={link.href} href={link.href} className="text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">
              {link.label}
            </a>
          ))}
        </nav>

        <div className="hidden items-center gap-2 md:flex">
          <ThemeToggle />
          <Button variant="ghost" asChild>
            <Link href={ROUTES.login}>Log in</Link>
          </Button>
          <Button asChild>
            <Link href={ROUTES.register}>Get Started</Link>
          </Button>
        </div>

        <button
          className="md:hidden"
          aria-label="Toggle menu"
          onClick={() => setIsOpen((prev) => !prev)}
        >
          {isOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </div>

      {isOpen && (
        <div className="border-t border-border/60 px-6 py-4 md:hidden">
          <nav className="flex flex-col gap-3">
            {NAV_LINKS.map((link) => (
              <a key={link.href} href={link.href} className="text-sm font-medium text-muted-foreground" onClick={() => setIsOpen(false)}>
                {link.label}
              </a>
            ))}
          </nav>
          <div className="mt-4 flex flex-col gap-2">
            <Button variant="outline" asChild>
              <Link href={ROUTES.login}>Log in</Link>
            </Button>
            <Button asChild>
              <Link href={ROUTES.register}>Get Started</Link>
            </Button>
          </div>
        </div>
      )}
    </header>
  );
}
