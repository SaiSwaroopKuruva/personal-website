package finadvisor.repository;

import finadvisor.entity.MutualFundDataSync;
import finadvisor.entity.SyncType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MutualFundDataSyncRepository extends JpaRepository<MutualFundDataSync, UUID> {

    Optional<MutualFundDataSync> findFirstByProviderAndSyncTypeOrderByStartedAtDesc(String provider, SyncType syncType);
}
