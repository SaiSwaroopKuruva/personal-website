package finadvisor.repository;

import finadvisor.entity.PasswordHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistoryEntry, UUID> {
    List<PasswordHistoryEntry> findTop5ByUser_IdOrderByCreatedAtDesc(UUID userId);
}
