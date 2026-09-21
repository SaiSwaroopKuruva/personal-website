package finadvisor.dto.mutualfund;

import java.time.Instant;

public record FavoriteFundResponse(MutualFundSummaryResponse fund, Instant favoritedAt) {
}
