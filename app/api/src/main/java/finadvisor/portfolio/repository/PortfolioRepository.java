package finadvisor.portfolio.repository;

import finadvisor.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

    List<Portfolio> findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(UUID userId);

    Optional<Portfolio> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);

    boolean existsByUserIdAndIsDefaultTrue(UUID userId);
}
