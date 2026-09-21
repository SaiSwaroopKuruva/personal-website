package finadvisor.controller;

import finadvisor.dto.PageResponse;
import finadvisor.dto.mutualfund.FavoriteFundResponse;
import finadvisor.dto.mutualfund.FavoriteStatusResponse;
import finadvisor.service.MutualFundFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mutual-funds")
@RequiredArgsConstructor
@Validated
@Tag(name = "Mutual Fund Favorites", description = "Authenticated users can save mutual funds to a favorites list")
public class MutualFundFavoriteController {

    private static final String SCHEME_CODE_PATTERN = "^[A-Za-z0-9._-]{1,50}$";

    private final MutualFundFavoriteService favoriteService;

    @GetMapping("/favorites")
    @Operation(summary = "List the current user's favorite mutual funds")
    public ResponseEntity<PageResponse<FavoriteFundResponse>> listFavorites(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(favoriteService.listFavorites(authentication.getName(), page, size));
    }

    @PostMapping("/{schemeCode}/favorite")
    @Operation(summary = "Add a mutual fund to favorites")
    public ResponseEntity<FavoriteStatusResponse> addFavorite(
            Authentication authentication,
            @PathVariable @Pattern(regexp = SCHEME_CODE_PATTERN) String schemeCode) {
        return ResponseEntity.ok(favoriteService.addFavorite(authentication.getName(), schemeCode));
    }

    @DeleteMapping("/{schemeCode}/favorite")
    @Operation(summary = "Remove a mutual fund from favorites")
    public ResponseEntity<FavoriteStatusResponse> removeFavorite(
            Authentication authentication,
            @PathVariable @Pattern(regexp = SCHEME_CODE_PATTERN) String schemeCode) {
        return ResponseEntity.ok(favoriteService.removeFavorite(authentication.getName(), schemeCode));
    }
}
