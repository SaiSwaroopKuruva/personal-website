package finadvisor.stock.service.impl;

import finadvisor.stock.entity.ExchangeStatus;
import finadvisor.stock.entity.PriceDataType;
import finadvisor.stock.entity.Stock;
import finadvisor.stock.entity.StockDataSync;
import finadvisor.stock.entity.StockExchange;
import finadvisor.stock.entity.StockPrice;
import finadvisor.stock.entity.StockStatus;
import finadvisor.stock.entity.StockSyncStatus;
import finadvisor.stock.entity.StockSyncType;
import finadvisor.stock.dto.StockSyncResultResponse;
import finadvisor.stock.provider.MarketCandle;
import finadvisor.stock.provider.StockProviderCallExecutor;
import finadvisor.stock.provider.ProviderResponse;
import finadvisor.stock.provider.ProviderStockDetails;
import finadvisor.stock.provider.StockMarketDataProvider;
import finadvisor.stock.provider.StockProviderException;
import finadvisor.stock.repository.StockDataSyncRepository;
import finadvisor.stock.repository.StockExchangeRepository;
import finadvisor.stock.repository.StockPriceRepository;
import finadvisor.stock.repository.StockRepository;
import finadvisor.stock.service.StockDataSyncService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class StockDataSyncServiceImpl implements StockDataSyncService {

    private static final Logger log = Logger.getLogger(StockDataSyncServiceImpl.class.getName());

    private final Optional<StockMarketDataProvider> provider;
    private final StockProviderCallExecutor providerCallExecutor;
    private final StockExchangeRepository exchangeRepository;
    private final StockRepository stockRepository;
    private final StockPriceRepository priceRepository;
    private final StockDataSyncRepository syncLogRepository;

    public StockDataSyncServiceImpl(Optional<StockMarketDataProvider> provider, StockProviderCallExecutor providerCallExecutor,
                                     StockExchangeRepository exchangeRepository, StockRepository stockRepository,
                                     StockPriceRepository priceRepository, StockDataSyncRepository syncLogRepository) {
        this.provider = provider;
        this.providerCallExecutor = providerCallExecutor;
        this.exchangeRepository = exchangeRepository;
        this.stockRepository = stockRepository;
        this.priceRepository = priceRepository;
        this.syncLogRepository = syncLogRepository;
    }

    @Override
    @Transactional
    public StockSyncResultResponse syncInstruments() {
        Instant startedAt = Instant.now();
        if (provider.isEmpty()) {
            return persistLog(StockSyncType.INSTRUMENT_SYNC, startedAt, 0, 0, StockSyncStatus.FAILED, "No stock provider configured");
        }
        int processed = 0;
        int failed = 0;
        String errorMessage = null;
        try {
            ProviderResponse<List<ProviderStockDetails>> response = providerCallExecutor.executeWithRetry(
                    "syncInstruments", () -> provider.get().syncInstruments());
            if (!response.success()) {
                return persistLog(StockSyncType.INSTRUMENT_SYNC, startedAt, 0, 0, StockSyncStatus.FAILED, response.errorMessage());
            }
            for (ProviderStockDetails details : response.data()) {
                try {
                    upsertStock(details);
                    processed++;
                } catch (Exception ex) {
                    failed++;
                    log.log(Level.WARNING, "Failed to upsert stock " + details.symbol(), ex);
                }
            }
        } catch (StockProviderException ex) {
            errorMessage = ex.getMessage();
            log.log(Level.SEVERE, "Stock instrument sync failed", ex);
        }
        StockSyncStatus status = resolveStatus(processed, failed, errorMessage);
        return persistLog(StockSyncType.INSTRUMENT_SYNC, startedAt, processed, failed, status, errorMessage);
    }

    @Override
    @Transactional
    public StockSyncResultResponse syncEndOfDayPrices() {
        Instant startedAt = Instant.now();
        if (provider.isEmpty()) {
            return persistLog(StockSyncType.EOD_SYNC, startedAt, 0, 0, StockSyncStatus.FAILED, "No stock provider configured");
        }
        int processed = 0;
        int failed = 0;
        LocalDate today = LocalDate.now();

        for (Stock stock : stockRepository.findByStatusNot(StockStatus.DELISTED)) {
            try {
                ProviderResponse<List<MarketCandle>> response = providerCallExecutor.executeWithRetry(
                        "getHistoricalPrices:" + stock.getSymbol(),
                        () -> provider.get().getHistoricalPrices(stock.getSymbol(), today.minusDays(7), today));
                if (!response.success() || response.data().isEmpty()) {
                    failed++;
                    continue;
                }
                MarketCandle latest = response.data().get(response.data().size() - 1);
                boolean exists = priceRepository.existsByStock_IdAndPriceTimestampAndDataType(
                        stock.getId(), latest.timestamp(), PriceDataType.END_OF_DAY);
                if (!exists) {
                    priceRepository.save(StockPrice.builder()
                            .stock(stock)
                            .priceTimestamp(latest.timestamp())
                            .open(latest.open()).high(latest.high()).low(latest.low()).close(latest.close())
                            .lastTradedPrice(latest.close())
                            .volume(latest.volume())
                            .source("UPSTOX")
                            .dataType(PriceDataType.END_OF_DAY)
                            .build());
                }
                processed++;
            } catch (StockProviderException ex) {
                failed++;
                log.log(Level.WARNING, "EOD sync failed for " + stock.getSymbol(), ex);
            }
        }
        return persistLog(StockSyncType.EOD_SYNC, startedAt, processed, failed, resolveStatus(processed, failed, null), null);
    }

    @Override
    public List<StockSyncResultResponse> syncAll() {
        return List.of(syncInstruments(), syncEndOfDayPrices());
    }

    private void upsertStock(ProviderStockDetails details) {
        StockExchange exchange = exchangeRepository.findByCode(details.exchange() != null ? details.exchange() : "NSE")
                .orElseGet(() -> exchangeRepository.save(StockExchange.builder()
                        .code(details.exchange() != null ? details.exchange() : "NSE")
                        .name(details.exchange() != null ? details.exchange() : "NSE")
                        .country("IN").timezone("Asia/Kolkata").status(ExchangeStatus.ACTIVE).build()));

        Stock stock = stockRepository.findByInstrumentKey(details.instrumentKey())
                .orElseGet(() -> Stock.builder().instrumentKey(details.instrumentKey()).status(StockStatus.ACTIVE).build());

        stock.setSymbol(details.symbol());
        stock.setExchange(exchange);
        stock.setIsin(details.isin());
        stock.setCompanyName(details.companyName());
        stock.setSector(details.sector());
        stock.setSeries(details.series());
        stock.setLotSize(details.lotSize());
        stock.setLastSyncedAt(Instant.now());
        stockRepository.save(stock);
    }

    private StockSyncStatus resolveStatus(int processed, int failed, String errorMessage) {
        if (errorMessage != null) {
            return StockSyncStatus.FAILED;
        }
        if (failed == 0) {
            return StockSyncStatus.SUCCESS;
        }
        return processed > 0 ? StockSyncStatus.PARTIAL_FAILURE : StockSyncStatus.FAILED;
    }

    private StockSyncResultResponse persistLog(StockSyncType type, Instant startedAt, int processed, int failed,
                                                StockSyncStatus status, String errorMessage) {
        Instant completedAt = Instant.now();
        StockDataSync entity = StockDataSync.builder()
                .provider(provider.map(StockMarketDataProvider::getProviderName).orElse("NONE"))
                .syncType(type).startedAt(startedAt).completedAt(completedAt)
                .recordsProcessed(processed).recordsFailed(failed).status(status).errorMessage(errorMessage)
                .build();
        syncLogRepository.save(entity);
        return new StockSyncResultResponse(type.name(), startedAt, completedAt, processed, failed, status.name(), errorMessage);
    }
}
