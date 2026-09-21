package finadvisor.service;

import finadvisor.dto.PageResponse;
import finadvisor.dto.mutualfund.FavoriteFundResponse;
import finadvisor.dto.mutualfund.FavoriteStatusResponse;

public interface MutualFundFavoriteService {

    FavoriteStatusResponse addFavorite(String userEmail, String schemeCode);

    FavoriteStatusResponse removeFavorite(String userEmail, String schemeCode);

    PageResponse<FavoriteFundResponse> listFavorites(String userEmail, int page, int size);
}
