package finadvisor.mutualfund.service.impl;

import finadvisor.dto.PageResponse;
import finadvisor.mutualfund.dto.FavoriteFundResponse;
import finadvisor.mutualfund.dto.FavoriteStatusResponse;
import finadvisor.mutualfund.entity.FundStatus;
import finadvisor.mutualfund.entity.MutualFund;
import finadvisor.entity.User;
import finadvisor.mutualfund.entity.UserMutualFundFavorite;
import finadvisor.mutualfund.exception.DuplicateFavoriteException;
import finadvisor.mutualfund.exception.MutualFundNotFoundException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mutualfund.mapper.MutualFundMapper;
import finadvisor.mutualfund.repository.MutualFundRepository;
import finadvisor.mutualfund.repository.MutualFundReturnRepository;
import finadvisor.mutualfund.repository.UserMutualFundFavoriteRepository;
import finadvisor.repository.UserRepository;
import finadvisor.mutualfund.service.MutualFundFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MutualFundFavoriteServiceImpl implements MutualFundFavoriteService {

    private final UserMutualFundFavoriteRepository favoriteRepository;
    private final MutualFundRepository mutualFundRepository;
    private final MutualFundReturnRepository returnRepository;
    private final UserRepository userRepository;
    private final MutualFundMapper mapper;

    @Override
    public FavoriteStatusResponse addFavorite(String userEmail, String schemeCode) {
        User user = findUser(userEmail);
        MutualFund fund = findFund(schemeCode);

        if (favoriteRepository.existsByUser_IdAndMutualFund_Id(user.getId(), fund.getId())) {
            throw new DuplicateFavoriteException("This fund is already in your favorites");
        }

        favoriteRepository.save(UserMutualFundFavorite.builder().user(user).mutualFund(fund).build());
        return new FavoriteStatusResponse(schemeCode, true);
    }

    @Override
    public FavoriteStatusResponse removeFavorite(String userEmail, String schemeCode) {
        User user = findUser(userEmail);
        MutualFund fund = findFund(schemeCode);
        favoriteRepository.deleteByUser_IdAndMutualFund_Id(user.getId(), fund.getId());
        return new FavoriteStatusResponse(schemeCode, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FavoriteFundResponse> listFavorites(String userEmail, int page, int size) {
        User user = findUser(userEmail);
        Page<UserMutualFundFavorite> favorites = favoriteRepository.findByUser_IdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : Math.min(size, 100)));

        var fundIds = favorites.getContent().stream().map(f -> f.getMutualFund().getId()).toList();
        Map<UUID, Map<finadvisor.mutualfund.entity.ReturnPeriod, BigDecimal>> returnsByFund = fundIds.isEmpty() ? Map.of()
                : returnRepository.findByMutualFund_IdIn(fundIds).stream()
                        .collect(Collectors.groupingBy(r -> r.getMutualFund().getId(),
                                Collectors.toMap(finadvisor.mutualfund.entity.MutualFundReturn::getReturnPeriod,
                                        finadvisor.mutualfund.entity.MutualFundReturn::getReturnPercentage, (a, b) -> a)));

        return PageResponse.of(favorites.map(favorite -> new FavoriteFundResponse(
                mapper.toSummary(favorite.getMutualFund(), returnsByFund.getOrDefault(favorite.getMutualFund().getId(), Map.of()), true),
                favorite.getCreatedAt())));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private MutualFund findFund(String schemeCode) {
        return mutualFundRepository.findBySchemeCodeAndStatusNot(schemeCode, FundStatus.INACTIVE)
                .orElseThrow(() -> new MutualFundNotFoundException("Mutual fund not found: " + schemeCode));
    }
}
