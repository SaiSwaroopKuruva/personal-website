package finadvisor.mutualfund.repository;

import finadvisor.mutualfund.entity.UserMutualFundFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMutualFundFavoriteRepository extends JpaRepository<UserMutualFundFavorite, UUID> {

    boolean existsByUser_IdAndMutualFund_Id(UUID userId, UUID mutualFundId);

    Optional<UserMutualFundFavorite> findByUser_IdAndMutualFund_Id(UUID userId, UUID mutualFundId);

    Page<UserMutualFundFavorite> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    void deleteByUser_IdAndMutualFund_Id(UUID userId, UUID mutualFundId);

    @Query("select f.mutualFund.id from UserMutualFundFavorite f where f.user.id = :userId and f.mutualFund.id in :fundIds")
    List<UUID> findFavoritedFundIds(UUID userId, List<UUID> fundIds);
}
