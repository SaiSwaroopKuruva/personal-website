package finadvisor.stock.repository;

import finadvisor.stock.entity.UserStockWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStockWatchlistRepository extends JpaRepository<UserStockWatchlist, UUID> {

    boolean existsByUser_IdAndStock_Id(UUID userId, UUID stockId);

    Optional<UserStockWatchlist> findByUser_IdAndStock_Id(UUID userId, UUID stockId);

    List<UserStockWatchlist> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    void deleteByUser_IdAndStock_Id(UUID userId, UUID stockId);
}
