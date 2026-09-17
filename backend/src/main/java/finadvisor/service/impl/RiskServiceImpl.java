package finadvisor.service.impl;

import finadvisor.dto.PageResponse;
import finadvisor.dto.risk.RiskAssessmentResponse;
import finadvisor.dto.risk.RiskOptionResponse;
import finadvisor.dto.risk.RiskQuestionResponse;
import finadvisor.dto.risk.RiskSubmissionRequest;
import finadvisor.entity.RiskAssessmentResult;
import finadvisor.entity.RiskLevel;
import finadvisor.entity.RiskProfile;
import finadvisor.entity.RiskRecommendation;
import finadvisor.entity.User;
import finadvisor.events.AuditEvent;
import finadvisor.exception.RiskAssessmentNotFoundException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mapper.RiskMapper;
import finadvisor.repository.RiskAssessmentResultRepository;
import finadvisor.repository.UserRepository;
import finadvisor.risk.RiskEngine;
import finadvisor.risk.RiskQuestionCatalog;
import finadvisor.security.RequestMetadataProvider;
import finadvisor.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskServiceImpl implements RiskService {

    private final UserRepository userRepository;
    private final RiskAssessmentResultRepository riskAssessmentResultRepository;
    private final RiskQuestionCatalog riskQuestionCatalog;
    private final RiskEngine riskEngine;
    private final RiskMapper riskMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestMetadataProvider requestMetadataProvider;

    @Override
    @Transactional(readOnly = true)
    public List<RiskQuestionResponse> getQuestions() {
        return riskQuestionCatalog.getQuestions().stream()
                .map(question -> new RiskQuestionResponse(
                        question.code(),
                        question.text(),
                        question.helpText(),
                        question.options().stream()
                                .map(option -> new RiskOptionResponse(option.code(), option.label(), option.score()))
                                .toList()))
                .toList();
    }

    @Override
    public RiskAssessmentResponse submit(String email, RiskSubmissionRequest request) {
        User user = findUser(email);
        int score = riskEngine.calculateScore(request.answers());
        RiskLevel riskLevel = riskEngine.resolveRiskLevel(score);
        RiskRecommendation recommendation = riskEngine.buildRecommendation(riskLevel);

        RiskAssessmentResult result = riskAssessmentResultRepository.save(RiskAssessmentResult.builder()
                .user(user)
                .score(score)
                .riskLevel(riskLevel)
                .recommendation(recommendation)
                .build());

        user.setRiskProfile(mapToLegacyRiskProfile(riskLevel));
        userRepository.save(user);

        eventPublisher.publishEvent(new AuditEvent(user.getId(), "RISK_ASSESSMENT_SUBMITTED",
                "Risk assessment submitted with score " + score + " (" + riskLevel + ")",
                requestMetadataProvider.current().ipAddress()));

        return riskMapper.toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAssessmentResponse getLatest(String email) {
        User user = findUser(email);
        RiskAssessmentResult result = riskAssessmentResultRepository.findFirstByUser_IdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new RiskAssessmentNotFoundException("No risk assessment has been completed yet"));
        return riskMapper.toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RiskAssessmentResponse> getHistory(String email, int page, int size) {
        User user = findUser(email);
        var result = riskAssessmentResultRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));
        return PageResponse.of(result.map(riskMapper::toResponse));
    }

    /** Maps the fine-grained 5-tier risk level to the coarse 3-tier profile used elsewhere in the platform. */
    private RiskProfile mapToLegacyRiskProfile(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case CONSERVATIVE, MODERATELY_CONSERVATIVE -> RiskProfile.CONSERVATIVE;
            case BALANCED -> RiskProfile.MODERATE;
            case GROWTH, AGGRESSIVE -> RiskProfile.AGGRESSIVE;
        };
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
