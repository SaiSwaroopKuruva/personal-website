package finadvisor.mutualfund.repository;

import finadvisor.mutualfund.entity.MutualFundManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MutualFundManagerRepository extends JpaRepository<MutualFundManager, UUID> {

    List<MutualFundManager> findByMutualFund_IdOrderByActiveDescJoiningDateDesc(UUID mutualFundId);

    void deleteByMutualFund_Id(UUID mutualFundId);
}
