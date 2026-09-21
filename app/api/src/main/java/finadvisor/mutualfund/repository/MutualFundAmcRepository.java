package finadvisor.mutualfund.repository;

import finadvisor.mutualfund.entity.FundStatus;
import finadvisor.mutualfund.entity.MutualFundAmc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MutualFundAmcRepository extends JpaRepository<MutualFundAmc, UUID> {

    Optional<MutualFundAmc> findByCode(String code);

    List<MutualFundAmc> findAllByStatusOrderByNameAsc(FundStatus status);
}
