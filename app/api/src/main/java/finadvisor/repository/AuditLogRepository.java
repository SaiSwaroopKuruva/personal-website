package finadvisor.repository;

import finadvisor.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findByUser_IdAndActionInOrderByCreatedAtDesc(UUID userId, Collection<String> actions, Pageable pageable);
}
