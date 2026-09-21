package finadvisor.mutualfund.dto;

import java.time.Instant;

public record FavoriteFundResponse(MutualFundSummaryResponse fund, Instant favoritedAt) {
}
