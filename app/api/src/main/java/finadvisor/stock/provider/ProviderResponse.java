package finadvisor.stock.provider;

/** Uniform envelope returned by every {@link StockMarketDataProvider} call - mirrors the mutual fund provider pattern. */
public record ProviderResponse<T>(boolean success, T data, String errorMessage, String providerName) {

    public static <T> ProviderResponse<T> ok(T data, String providerName) {
        return new ProviderResponse<>(true, data, null, providerName);
    }

    public static <T> ProviderResponse<T> failure(String errorMessage, String providerName) {
        return new ProviderResponse<>(false, null, errorMessage, providerName);
    }
}
