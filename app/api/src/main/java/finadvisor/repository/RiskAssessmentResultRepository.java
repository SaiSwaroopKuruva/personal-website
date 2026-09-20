package finadvisor.repository;

import finadvisor.entity.RiskAssessmentResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentResultRepository extends JpaRepository<RiskAssessmentResult, UUID> {
    Optional<RiskAssessmentResult> findFirstByUser_IdOrderByCreatedAtDesc(UUID userId);

    Page<RiskAssessmentResult> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    void deleteByUser_Id(UUID userId);
}
