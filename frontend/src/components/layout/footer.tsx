import Link from "next/link";
import { TrendingUp } from "lucide-react";
import { ROUTES } from "@/lib/constants";

const FOOTER_LINKS = {
  Product: [
    { label: "Features", href: "#features" },
    { label: "How It Works", href: "#how-it-works" },
    { label: "Investment Categories", href: "#investments" },
  ],
  Company: [
    { label: "Why Choose Us", href: "#why-us" },
    { label: "Testimonials", href: "#testimonials" },
    { label: "FAQ", href: "#faq" },
  ],
  Account: [
    { label: "Log in", href: ROUTES.login },
    { label: "Create account", href: ROUTES.register },
  ],
};

export function Footer() {
  return (
    <footer className="border-t border-border/60 bg-muted/30">
      <div className="container grid gap-10 py-12 md:grid-cols-4">
        <div>
          <div className="flex items-center gap-2 font-semibold">
            <TrendingUp className="h-6 w-6 text-primary" />
            <span>NiveshPath</span>
          </div>
          <p className="mt-3 max-w-xs text-sm text-muted-foreground">
            Thoughtful, transparent financial advisory built for every stage of your investing journey in India.
          </p>
        </div>

        {Object.entries(FOOTER_LINKS).map(([section, links]) => (
          <div key={section}>
            <h4 className="text-sm font-semibold">{section}</h4>
            <ul className="mt-3 space-y-2">
              {links.map((link) => (
                <li key={link.href}>
                  <Link href={link.href} className="text-sm text-muted-foreground hover:text-foreground">
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <div className="border-t border-border/60 py-6 text-center text-xs text-muted-foreground">
        © {new Date().getFullYear()} NiveshPath. All rights reserved. Investments are subject to market risks.
      </div>
    </footer>
  );
}
