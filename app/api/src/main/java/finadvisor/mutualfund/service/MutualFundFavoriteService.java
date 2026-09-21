package finadvisor.mutualfund.service;

import finadvisor.dto.PageResponse;
import finadvisor.mutualfund.dto.FavoriteFundResponse;
import finadvisor.mutualfund.dto.FavoriteStatusResponse;

public interface MutualFundFavoriteService {

    FavoriteStatusResponse addFavorite(String userEmail, String schemeCode);

    FavoriteStatusResponse removeFavorite(String userEmail, String schemeCode);

    PageResponse<FavoriteFundResponse> listFavorites(String userEmail, int page, int size);
}
