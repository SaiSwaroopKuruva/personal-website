package finadvisor.mutualfund.dto;

import java.util.List;

public record NavHistoryResponse(String schemeCode, String interval, List<NavPointResponse> points) {
}
