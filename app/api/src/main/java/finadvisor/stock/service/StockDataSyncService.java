package finadvisor.stock.service;

import finadvisor.stock.dto.StockSyncResultResponse;

import java.util.List;

public interface StockDataSyncService {

    StockSyncResultResponse syncInstruments();

    StockSyncResultResponse syncEndOfDayPrices();

    List<StockSyncResultResponse> syncAll();
}
