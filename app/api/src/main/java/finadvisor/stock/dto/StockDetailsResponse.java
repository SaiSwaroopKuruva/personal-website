package finadvisor.stock.dto;

public record StockDetailsResponse(
        String symbol,
        String exchange,
        String isin,
        String companyName,
        String sector,
        String series,
        Integer lotSize
) {
}
