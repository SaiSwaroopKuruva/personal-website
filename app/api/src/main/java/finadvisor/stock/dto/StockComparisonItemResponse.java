package finadvisor.stock.dto;

public record StockComparisonItemResponse(
        String symbol,
        String companyName,
        String exchange,
        String sector,
        String series,
        Integer lotSize,
        StockQuoteResponse quote
) {
}
