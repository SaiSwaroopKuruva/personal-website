package finadvisor.dto.mutualfund;

import java.util.List;

public record NavHistoryResponse(String schemeCode, String interval, List<NavPointResponse> points) {
}
