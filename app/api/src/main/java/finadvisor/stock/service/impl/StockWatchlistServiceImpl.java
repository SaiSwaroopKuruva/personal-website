package finadvisor.stock.service.impl;

import finadvisor.entity.User;
import finadvisor.exception.UserNotFoundException;
import finadvisor.marketdata.ErrorCategory;
import finadvisor.repository.UserRepository;
import finadvisor.stock.dto.StockQuoteResponse;
import finadvisor.stock.dto.WatchlistItemResponse;
import finadvisor.stock.dto.WatchlistStatusResponse;
import finadvisor.stock.entity.ExchangeStatus;
import finadvisor.stock.entity.Stock;
import finadvisor.stock.entity.StockExchange;
import finadvisor.stock.entity.StockStatus;
import finadvisor.stock.entity.UserStockWatchlist;
import finadvisor.stock.exception.DuplicateWatchlistException;
import finadvisor.stock.exception.StockNotFoundException;
import finadvisor.stock.provider.ProviderResponse;
import finadvisor.stock.provider.ProviderStockDetails;
import finadvisor.stock.provider.StockMarketDataProvider;
import finadvisor.stock.provider.StockProviderCallExecutor;
import finadvisor.stock.provider.StockProviderException;
import finadvisor.stock.repository.StockExchangeRepository;
import finadvisor.stock.repository.StockRepository;
import finadvisor.stock.repository.UserStockWatchlistRepository;
import finadvisor.stock.service.StockService;
import finadvisor.stock.service.StockWatchlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Transactional
public class StockWatchlistServiceImpl implements StockWatchlistService {

    private static final Logger log = Logger.getLogger(StockWatchlistServiceImpl.class.getName());

    private final UserStockWatchlistRepository watchlistRepository;
    private final StockRepository stockRepository;
    private final StockExchangeRepository exchangeRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final Optional<StockMarketDataProvider> provider;
    private final StockProviderCallExecutor providerCallExecutor;

    public StockWatchlistServiceImpl(UserStockWatchlistRepository watchlistRepository, StockRepository stockRepository,
                                      StockExchangeRepository exchangeRepository, UserRepository userRepository,
                                      StockService stockService, Optional<StockMarketDataProvider> provider,
                                      StockProviderCallExecutor providerCallExecutor) {
        this.watchlistRepository = watchlistRepository;
        this.stockRepository = stockRepository;
        this.exchangeRepository = exchangeRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
        this.provider = provider;
        this.providerCallExecutor = providerCallExecutor;
    }

    @Override
    public WatchlistStatusResponse addToWatchlist(String userEmail, String symbol) {
        User user = findUser(userEmail);
        Stock stock = resolveStock(symbol);

        if (watchlistRepository.existsByUser_IdAndStock_Id(user.getId(), stock.getId())) {
            throw new DuplicateWatchlistException("This stock is already in your watchlist");
        }
        watchlistRepository.save(UserStockWatchlist.builder().user(user).stock(stock).build());
        return new WatchlistStatusResponse(stock.getSymbol(), true);
    }

    @Override
    public WatchlistStatusResponse removeFromWatchlist(String userEmail, String symbol) {
        User user = findUser(userEmail);
        Stock stock = resolveStock(symbol);
        watchlistRepository.deleteByUser_IdAndStock_Id(user.getId(), stock.getId());
        return new WatchlistStatusResponse(stock.getSymbol(), false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> listWatchlist(String userEmail) {
        User user = findUser(userEmail);
        return watchlistRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(entry -> {
                    Stock stock = entry.getStock();
                    StockQuoteResponse quote = fetchQuoteBestEffort(stock.getSymbol());
                    return new WatchlistItemResponse(stock.getSymbol(), stock.getCompanyName(),
                            stock.getExchange().getCode(), quote, entry.getCreatedAt());
                })
                .toList();
    }

    private StockQuoteResponse fetchQuoteBestEffort(String symbol) {
        try {
            return stockService.getCurrentPrice(symbol);
        } catch (RuntimeException ex) {
            log.log(Level.FINE, "Could not fetch live quote for watchlist symbol " + symbol, ex);
            return null;
        }
    }

    /** Finds the locally-synced stock, or resolves it live from the provider and persists it (mirrors the sync service's upsert pattern). */
    private Stock resolveStock(String symbol) {
        return stockRepository.findBySymbolIgnoreCaseAndStatusNot(symbol, StockStatus.DELISTED)
                .orElseGet(() -> upsertFromProvider(symbol));
    }

    private Stock upsertFromProvider(String symbol) {
        StockMarketDataProvider activeProvider = provider.orElseThrow(() -> new StockProviderException(
                "No stock market-data provider is configured (set STOCK_DATA_PROVIDER=UPSTOX)", ErrorCategory.PROVIDER_UNAVAILABLE));
        ProviderResponse<ProviderStockDetails> response = providerCallExecutor.executeWithRetry(
                "getStockDetails", () -> activeProvider.getStockDetails(symbol));
        if (!response.success()) {
            throw new StockNotFoundException(response.errorMessage());
        }
        ProviderStockDetails details = response.data();

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
        return stockRepository.save(stock);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
