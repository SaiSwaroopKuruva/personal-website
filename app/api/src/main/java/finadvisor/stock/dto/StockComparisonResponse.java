package finadvisor.stock.dto;

import java.util.List;

/** Side-by-side stock comparison (Part 22) - presents only fields backed by real provider data; no PE/PB/EPS
 * ranking or fabricated fundamentals (those endpoints remain 501 until a verified fundamentals source exists). */
public record StockComparisonResponse(List<StockComparisonItemResponse> stocks, String disclaimer, String dataLimitationNote) {
}
