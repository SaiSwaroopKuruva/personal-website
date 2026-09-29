package finadvisor.networth.repository;

import finadvisor.networth.entity.UserAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAssetRepository extends JpaRepository<UserAsset, UUID> {

    List<UserAsset> findByUserIdOrderByCreatedAtAsc(UUID userId);

    Optional<UserAsset> findByIdAndUserId(UUID id, UUID userId);
}
