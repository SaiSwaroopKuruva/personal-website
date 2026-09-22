package finadvisor.stock.dto;

import java.util.List;

public record CandleHistoryResponse(String symbol, String interval, List<CandleResponse> candles) {
}
