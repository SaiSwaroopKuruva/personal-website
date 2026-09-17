package finadvisor.repository;

import finadvisor.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    List<UserAddress> findByUser_IdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    Optional<UserAddress> findByIdAndUser_Id(UUID id, UUID userId);

    List<UserAddress> findByUser_IdAndIsDefaultTrue(UUID userId);
}
