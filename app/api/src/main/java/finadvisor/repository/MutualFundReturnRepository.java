package finadvisor.repository;

import finadvisor.entity.MutualFundReturn;
import finadvisor.entity.ReturnPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MutualFundReturnRepository extends JpaRepository<MutualFundReturn, UUID> {

    List<MutualFundReturn> findByMutualFund_Id(UUID mutualFundId);

    List<MutualFundReturn> findByMutualFund_IdIn(List<UUID> mutualFundIds);

    Optional<MutualFundReturn> findByMutualFund_IdAndReturnPeriod(UUID mutualFundId, ReturnPeriod returnPeriod);

    void deleteByMutualFund_IdAndReturnPeriod(UUID mutualFundId, ReturnPeriod returnPeriod);
}
