package finadvisor.stock.service.impl;

import finadvisor.entity.User;
import finadvisor.exception.UserNotFoundException;
import finadvisor.repository.UserRepository;
import finadvisor.stock.dto.StockQuoteResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockWatchlistServiceImplTest {

    @Mock
    private UserStockWatchlistRepository watchlistRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockExchangeRepository exchangeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockService stockService;

    @Mock
    private StockMarketDataProvider provider;

    @Mock
    private StockProviderCallExecutor providerCallExecutor;

    private StockWatchlistServiceImpl service;

    private User user;
    private Stock stock;

    @BeforeEach
    void setUp() {
        service = new StockWatchlistServiceImpl(watchlistRepository, stockRepository, exchangeRepository,
                userRepository, stockService, Optional.of(provider), providerCallExecutor);
        user = User.builder().id(UUID.randomUUID()).email("investor@example.com").build();
        stock = Stock.builder().id(UUID.randomUUID()).symbol("INFY").companyName("Infosys Limited")
                .instrumentKey("NSE_EQ|INE009A01021").status(StockStatus.ACTIVE)
                .exchange(StockExchange.builder().code("NSE").name("NSE").status(ExchangeStatus.ACTIVE).build())
                .build();
    }

    @Test
    void addToWatchlist_shouldSaveWhenAlreadySynced() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbolIgnoreCaseAndStatusNot("INFY", StockStatus.DELISTED)).thenReturn(Optional.of(stock));
        when(watchlistRepository.existsByUser_IdAndStock_Id(user.getId(), stock.getId())).thenReturn(false);

        var response = service.addToWatchlist(user.getEmail(), "INFY");

        assertThat(response.inWatchlist()).isTrue();
        verify(watchlistRepository).save(any(UserStockWatchlist.class));
    }

    @Test
    void addToWatchlist_shouldRejectDuplicate() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbolIgnoreCaseAndStatusNot("INFY", StockStatus.DELISTED)).thenReturn(Optional.of(stock));
        when(watchlistRepository.existsByUser_IdAndStock_Id(user.getId(), stock.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.addToWatchlist(user.getEmail(), "INFY"))
                .isInstanceOf(DuplicateWatchlistException.class);
        verify(watchlistRepository, never()).save(any());
    }

    @Test
    void addToWatchlist_shouldResolveFromProviderWhenNotYetSynced() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbolIgnoreCaseAndStatusNot("TCS", StockStatus.DELISTED)).thenReturn(Optional.empty());
        ProviderStockDetails details = new ProviderStockDetails("TCS", "NSE", "NSE_EQ|INE467B01029", "INE467B01029",
                "Tata Consultancy Services", null, "EQ", 1);
        when(providerCallExecutor.executeWithRetry(any(), any())).thenAnswer(inv -> {
            Supplier<ProviderResponse<ProviderStockDetails>> supplier = inv.getArgument(1);
            return supplier.get();
        });
        when(provider.getStockDetails("TCS")).thenReturn(ProviderResponse.ok(details, "UPSTOX"));
        when(exchangeRepository.findByCode("NSE")).thenReturn(Optional.of(stock.getExchange()));
        when(stockRepository.findByInstrumentKey("NSE_EQ|INE467B01029")).thenReturn(Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));
        when(watchlistRepository.existsByUser_IdAndStock_Id(any(), any())).thenReturn(false);

        var response = service.addToWatchlist(user.getEmail(), "TCS");

        assertThat(response.symbol()).isEqualTo("TCS");
        assertThat(response.inWatchlist()).isTrue();
    }

    @Test
    void addToWatchlist_shouldThrowWhenProviderUnavailable() {
        StockWatchlistServiceImpl noProviderService = new StockWatchlistServiceImpl(watchlistRepository, stockRepository,
                exchangeRepository, userRepository, stockService, Optional.empty(), providerCallExecutor);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbolIgnoreCaseAndStatusNot("TCS", StockStatus.DELISTED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noProviderService.addToWatchlist(user.getEmail(), "TCS"))
                .isInstanceOf(StockProviderException.class);
    }

    @Test
    void addToWatchlist_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addToWatchlist("missing@example.com", "INFY"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void removeFromWatchlist_shouldDeleteAndReturnFalseStatus() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbolIgnoreCaseAndStatusNot("INFY", StockStatus.DELISTED)).thenReturn(Optional.of(stock));

        var response = service.removeFromWatchlist(user.getEmail(), "INFY");

        assertThat(response.inWatchlist()).isFalse();
        verify(watchlistRepository).deleteByUser_IdAndStock_Id(user.getId(), stock.getId());
    }

    @Test
    void listWatchlist_shouldReturnEntriesWithBestEffortQuotes() {
        UserStockWatchlist entry = UserStockWatchlist.builder().id(UUID.randomUUID()).user(user).stock(stock).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(watchlistRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())).thenReturn(java.util.List.of(entry));
        when(stockService.getCurrentPrice("INFY")).thenThrow(new StockNotFoundException("unavailable"));

        var response = service.listWatchlist(user.getEmail());

        assertThat(response).hasSize(1);
        assertThat(response.get(0).symbol()).isEqualTo("INFY");
        assertThat((StockQuoteResponse) response.get(0).quote()).isNull();
    }
}
