package finadvisor.mutualfund.util;

/** Reusable investor-facing disclaimer copy for mutual fund and calculator responses (Part 40). */
public final class FinancialDisclaimers {

    public static final String MUTUAL_FUND_GENERAL =
            "Mutual fund investments are subject to market risks. Past performance is not indicative of, "
                    + "and does not guarantee, future returns. The information shown is for educational and "
                    + "informational purposes only and should not be treated as personalized investment advice.";

    public static final String CALCULATOR_ESTIMATE =
            "This is an estimate based on the assumed constant annual return you entered, not a guaranteed or "
                    + "predicted outcome. Actual returns from mutual fund investments fluctuate with market conditions "
                    + "and may be lower or higher than shown. This tool does not constitute investment advice.";

    public static final String COMPARISON =
            "The figures below are factual metrics as of the dates shown and are not a ranking or recommendation. "
                    + "Mutual fund investments are subject to market risks; past performance does not guarantee future returns.";

    private FinancialDisclaimers() {
    }
}
