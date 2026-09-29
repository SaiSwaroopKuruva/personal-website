package finadvisor.networth.dto;

import java.util.List;

/**
 * Trend derived from portfolio valuation snapshots (investment leg) combined with the user's CURRENT
 * manual assets/liabilities (Part 15) - manual entries have no historical snapshot of their own, so
 * every point uses today's manual values; only the investment leg genuinely varies by date.
 */
public record NetWorthHistoryResponse(
        List<NetWorthHistoryPoint> points,
        String limitationMessage
) {
}
