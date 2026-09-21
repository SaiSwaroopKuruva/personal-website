package finadvisor.repository;

import finadvisor.entity.MutualFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MutualFundRepository extends JpaRepository<MutualFund, UUID>, JpaSpecificationExecutor<MutualFund> {

    Optional<MutualFund> findBySchemeCodeAndStatusNot(String schemeCode, finadvisor.entity.FundStatus excludedStatus);

    Optional<MutualFund> findBySchemeCode(String schemeCode);

    List<MutualFund> findBySchemeCodeInAndStatusNot(List<String> schemeCodes, finadvisor.entity.FundStatus excludedStatus);

    List<MutualFund> findByStatusNot(finadvisor.entity.FundStatus excludedStatus);

    boolean existsByIsin(String isin);
}
