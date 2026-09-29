package finadvisor.stock.service;

import finadvisor.stock.dto.WatchlistItemResponse;
import finadvisor.stock.dto.WatchlistStatusResponse;

import java.util.List;

public interface StockWatchlistService {

    WatchlistStatusResponse addToWatchlist(String userEmail, String symbol);

    WatchlistStatusResponse removeFromWatchlist(String userEmail, String symbol);

    List<WatchlistItemResponse> listWatchlist(String userEmail);
}
