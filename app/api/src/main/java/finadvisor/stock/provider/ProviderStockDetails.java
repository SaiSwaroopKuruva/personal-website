package finadvisor.stock.provider;

/** Basic descriptive/reference data for a tradable equity - never includes live pricing (see {@link MarketQuote}). */
public record ProviderStockDetails(
        String symbol,
        String exchange,
        String instrumentKey,
        String isin,
        String companyName,
        String sector,
        String series,
        Integer lotSize
) {
}
