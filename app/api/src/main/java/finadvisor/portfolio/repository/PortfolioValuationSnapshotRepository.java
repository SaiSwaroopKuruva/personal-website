package finadvisor.portfolio.repository;

import finadvisor.portfolio.entity.PortfolioValuationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioValuationSnapshotRepository extends JpaRepository<PortfolioValuationSnapshot, UUID> {

    Optional<PortfolioValuationSnapshot> findByPortfolioIdAndSnapshotDate(UUID portfolioId, LocalDate snapshotDate);

    List<PortfolioValuationSnapshot> findByPortfolioIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            UUID portfolioId, LocalDate from, LocalDate to);

    List<PortfolioValuationSnapshot> findByPortfolioIdOrderBySnapshotDateAsc(UUID portfolioId);

    void deleteByPortfolioIdAndSnapshotDateGreaterThanEqual(UUID portfolioId, LocalDate fromDate);
}
