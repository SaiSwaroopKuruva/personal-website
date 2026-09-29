package finadvisor.stock.dto;

/** Response after adding/removing a stock from the current user's watchlist (mirrors FavoriteStatusResponse). */
public record WatchlistStatusResponse(String symbol, boolean inWatchlist) {
}
