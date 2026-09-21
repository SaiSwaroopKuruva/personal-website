package finadvisor.mutualfund.repository;

import finadvisor.mutualfund.entity.MutualFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MutualFundRepository extends JpaRepository<MutualFund, UUID>, JpaSpecificationExecutor<MutualFund> {

    Optional<MutualFund> findBySchemeCodeAndStatusNot(String schemeCode, finadvisor.mutualfund.entity.FundStatus excludedStatus);

    Optional<MutualFund> findBySchemeCode(String schemeCode);

    List<MutualFund> findBySchemeCodeInAndStatusNot(List<String> schemeCodes, finadvisor.mutualfund.entity.FundStatus excludedStatus);

    List<MutualFund> findByStatusNot(finadvisor.mutualfund.entity.FundStatus excludedStatus);

    boolean existsByIsin(String isin);
}
