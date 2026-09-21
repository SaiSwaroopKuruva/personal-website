package finadvisor.mutualfund.repository;

import finadvisor.mutualfund.entity.MutualFundHolding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface MutualFundHoldingRepository extends JpaRepository<MutualFundHolding, UUID>, JpaSpecificationExecutor<MutualFundHolding> {

    Page<MutualFundHolding> findByMutualFund_Id(UUID mutualFundId, Pageable pageable);

    Optional<MutualFundHolding> findFirstByMutualFund_IdOrderByAsOfDateDesc(UUID mutualFundId);

    void deleteByMutualFund_IdAndAsOfDate(UUID mutualFundId, LocalDate asOfDate);

    @org.springframework.data.jpa.repository.Query(
            "select max(h.asOfDate) from MutualFundHolding h where h.mutualFund.id = :mutualFundId")
    Optional<LocalDate> findLatestAsOfDate(UUID mutualFundId);
}
