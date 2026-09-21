package finadvisor.dto.mutualfund;

import java.util.List;

public record ComparisonResponse(List<ComparisonFundResponse> funds, String disclaimer) {
}
