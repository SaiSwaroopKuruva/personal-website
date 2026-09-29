package finadvisor.networth.repository;

import finadvisor.networth.entity.UserLiability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserLiabilityRepository extends JpaRepository<UserLiability, UUID> {

    List<UserLiability> findByUserIdOrderByCreatedAtAsc(UUID userId);

    Optional<UserLiability> findByIdAndUserId(UUID id, UUID userId);
}
