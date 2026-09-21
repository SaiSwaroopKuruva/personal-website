package finadvisor.mutualfund.dto;

import java.util.List;

public record ComparisonResponse(List<ComparisonFundResponse> funds, String disclaimer) {
}
