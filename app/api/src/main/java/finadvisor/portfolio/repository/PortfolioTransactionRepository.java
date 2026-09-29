package finadvisor.portfolio.repository;

import finadvisor.portfolio.entity.PortfolioTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioTransactionRepository extends JpaRepository<PortfolioTransaction, UUID>, JpaSpecificationExecutor<PortfolioTransaction> {

    List<PortfolioTransaction> findByPortfolioIdOrderByTransactionDateAscCreatedAtAsc(UUID portfolioId);

    Page<PortfolioTransaction> findByPortfolioId(UUID portfolioId, org.springframework.data.domain.Pageable pageable);

    Optional<PortfolioTransaction> findByIdAndPortfolioId(UUID id, UUID portfolioId);

    void deleteByPortfolioId(UUID portfolioId);
}
