package finadvisor.dto.mutualfund;

import java.util.List;

public record ReturnsResponse(String schemeCode, List<ReturnResponse> returns, String disclaimer) {
}
