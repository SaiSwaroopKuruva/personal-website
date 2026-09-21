package finadvisor.repository;

import finadvisor.entity.MutualFundNavHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MutualFundNavHistoryRepository extends JpaRepository<MutualFundNavHistory, UUID> {

    List<MutualFundNavHistory> findByMutualFund_IdAndNavDateBetweenOrderByNavDateAsc(UUID mutualFundId, LocalDate from, LocalDate to);

    Optional<MutualFundNavHistory> findFirstByMutualFund_IdAndNavDateLessThanEqualOrderByNavDateDesc(UUID mutualFundId, LocalDate onOrBefore);

    Optional<MutualFundNavHistory> findFirstByMutualFund_IdOrderByNavDateDesc(UUID mutualFundId);

    Optional<MutualFundNavHistory> findFirstByMutualFund_IdOrderByNavDateAsc(UUID mutualFundId);

    boolean existsByMutualFund_IdAndNavDate(UUID mutualFundId, LocalDate navDate);
}
