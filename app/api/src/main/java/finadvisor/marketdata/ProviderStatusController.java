package finadvisor.marketdata;

import finadvisor.mutualfund.config.MutualFundProviderProperties;
import finadvisor.mutualfund.provider.MutualFundDataProvider;
import finadvisor.stock.config.StockProviderProperties;
import finadvisor.stock.provider.StockMarketDataProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/** Public, credential-free provider health snapshot (Part 30). */
@RestController
@RequestMapping("/api/data-providers")
@Tag(name = "Data Providers", description = "Public, credential-free status of the stock and mutual fund market-data providers")
public class ProviderStatusController {

    private final ProviderHealthTracker healthTracker;
    private final Optional<StockMarketDataProvider> stockProvider;
    private final StockProviderProperties stockProviderProperties;
    private final MutualFundDataProvider mutualFundDataProvider;
    private final MutualFundProviderProperties mutualFundProviderProperties;

    public ProviderStatusController(ProviderHealthTracker healthTracker, Optional<StockMarketDataProvider> stockProvider,
                                     StockProviderProperties stockProviderProperties, MutualFundDataProvider mutualFundDataProvider,
                                     MutualFundProviderProperties mutualFundProviderProperties) {
        this.healthTracker = healthTracker;
        this.stockProvider = stockProvider;
        this.stockProviderProperties = stockProviderProperties;
        this.mutualFundDataProvider = mutualFundDataProvider;
        this.mutualFundProviderProperties = mutualFundProviderProperties;
    }

    @GetMapping("/status")
    @Operation(summary = "Get the connection status of the stock and mutual fund data providers")
    public ProviderStatusResponse status() {
        ProviderStatus stocks = stockProvider.isPresent()
                ? healthTracker.getStatus("stocks", stockProvider.get().getProviderName())
                : new ProviderStatus(stockProviderProperties.getName(), ProviderConnectionStatus.DISABLED, null, null,
                        "No stock provider configured (set STOCK_DATA_PROVIDER=UPSTOX)");

        ProviderStatus mutualFunds = mutualFundProviderProperties.isEnabled()
                ? healthTracker.getStatus("mutualFunds", mutualFundDataProvider.getProviderName())
                : new ProviderStatus(mutualFundDataProvider.getProviderName(), ProviderConnectionStatus.DISABLED, null, null,
                        "Scheduled sync disabled (MUTUAL_FUND_PROVIDER_ENABLED=false); provider still serves cached/DB data");

        return new ProviderStatusResponse(stocks, mutualFunds);
    }
}
