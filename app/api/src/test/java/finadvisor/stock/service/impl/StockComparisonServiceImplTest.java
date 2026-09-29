package finadvisor.stock.service.impl;

import finadvisor.stock.dto.StockDetailsResponse;
import finadvisor.stock.exception.InvalidStockComparisonRequestException;
import finadvisor.stock.exception.StockNotFoundException;
import finadvisor.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockComparisonServiceImplTest {

    @Mock
    private StockService stockService;

    private StockComparisonServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StockComparisonServiceImpl(stockService);
    }

    private StockDetailsResponse details(String symbol) {
        return new StockDetailsResponse(symbol, "NSE", "INE00000001", symbol + " Limited", null, "EQ", 1);
    }

    @Test
    void compare_shouldReturnStocksInRequestedOrder() {
        when(stockService.getDetails("TCS")).thenReturn(details("TCS"));
        when(stockService.getDetails("INFY")).thenReturn(details("INFY"));
        when(stockService.getCurrentPrice("TCS")).thenThrow(new StockNotFoundException("no quote"));
        when(stockService.getCurrentPrice("INFY")).thenThrow(new StockNotFoundException("no quote"));

        var response = service.compare(List.of("TCS", "INFY"));

        assertThat(response.stocks()).extracting("symbol").containsExactly("TCS", "INFY");
        assertThat(response.disclaimer()).isNotBlank();
        assertThat(response.dataLimitationNote()).isNotBlank();
    }

    @Test
    void compare_shouldRejectFewerThanTwoStocks() {
        assertThatThrownBy(() -> service.compare(List.of("TCS")))
                .isInstanceOf(InvalidStockComparisonRequestException.class);
    }

    @Test
    void compare_shouldRejectMoreThanFourStocks() {
        assertThatThrownBy(() -> service.compare(List.of("A", "B", "C", "D", "E")))
                .isInstanceOf(InvalidStockComparisonRequestException.class);
    }

    @Test
    void compare_shouldRejectUnknownSymbol() {
        when(stockService.getDetails("TCS")).thenReturn(details("TCS"));
        when(stockService.getDetails("MISSING")).thenThrow(new StockNotFoundException("Unknown"));

        assertThatThrownBy(() -> service.compare(List.of("TCS", "MISSING")))
                .isInstanceOf(InvalidStockComparisonRequestException.class);
    }

    @Test
    void compare_shouldDeduplicateSymbols() {
        assertThatThrownBy(() -> service.compare(List.of("TCS", "TCS")))
                .isInstanceOf(InvalidStockComparisonRequestException.class);
    }
}
