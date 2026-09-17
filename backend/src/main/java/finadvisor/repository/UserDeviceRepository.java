package finadvisor.repository;

import finadvisor.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
    List<UserDevice> findByUser_IdOrderByLastLoginDesc(UUID userId);

    Optional<UserDevice> findByIdAndUser_Id(UUID id, UUID userId);

    Optional<UserDevice> findByUser_IdAndIpAddressAndBrowser(UUID userId, String ipAddress, String browser);

    void deleteByUser_Id(UUID userId);
}
