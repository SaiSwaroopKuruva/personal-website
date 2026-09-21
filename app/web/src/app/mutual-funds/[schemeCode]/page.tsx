import type { Metadata } from "next";
import { MutualFundDetailsClient } from "@/components/mutual-funds/mutual-fund-details-client";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface FundSeoSummary {
  schemeName: string;
  amcName: string;
  category: string;
  riskLevel: string;
}

async function fetchFundForSeo(schemeCode: string): Promise<FundSeoSummary | null> {
  try {
    const res = await fetch(`${API_BASE_URL}/api/mutual-funds/${schemeCode}`, { next: { revalidate: 3600 } });
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

export async function generateMetadata({ params }: { params: Promise<{ schemeCode: string }> }): Promise<Metadata> {
  const { schemeCode } = await params;
  const fund = await fetchFundForSeo(schemeCode);

  if (!fund) {
    return { title: "Mutual Fund Details | FinAdvisor" };
  }

  const title = `${fund.schemeName} | Mutual Fund Details | FinAdvisor`;
  const description = `${fund.schemeName} by ${fund.amcName} - ${fund.category} fund, ${fund.riskLevel.replace(/_/g, " ").toLowerCase()} risk. View NAV, returns, holdings and fund manager details. Educational information only, not investment advice.`;

  return {
    title,
    description,
    openGraph: { title, description },
  };
}

export default async function MutualFundDetailsPage({ params }: { params: Promise<{ schemeCode: string }> }) {
  const { schemeCode } = await params;
  return <MutualFundDetailsClient schemeCode={schemeCode} />;
}
