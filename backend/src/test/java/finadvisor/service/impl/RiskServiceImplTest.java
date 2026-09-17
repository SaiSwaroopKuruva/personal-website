package finadvisor.service.impl;

import finadvisor.dto.risk.RiskAnswerRequest;
import finadvisor.dto.risk.RiskSubmissionRequest;
import finadvisor.entity.RiskAssessmentResult;
import finadvisor.entity.RiskLevel;
import finadvisor.entity.RiskProfile;
import finadvisor.entity.RiskRecommendation;
import finadvisor.entity.User;
import finadvisor.exception.RiskAssessmentNotFoundException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mapper.RiskMapper;
import finadvisor.repository.RiskAssessmentResultRepository;
import finadvisor.repository.UserRepository;
import finadvisor.risk.RiskEngine;
import finadvisor.risk.RiskQuestionCatalog;
import finadvisor.security.RequestMetadata;
import finadvisor.security.RequestMetadataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RiskAssessmentResultRepository riskAssessmentResultRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RequestMetadataProvider requestMetadataProvider;

    private RiskServiceImpl riskService;

    @BeforeEach
    void setUp() {
        riskService = new RiskServiceImpl(userRepository, riskAssessmentResultRepository,
                new RiskQuestionCatalog(), new RiskEngine(new RiskQuestionCatalog()), new RiskMapper(),
                eventPublisher, requestMetadataProvider);
        lenient().when(requestMetadataProvider.current()).thenReturn(new RequestMetadata("127.0.0.1", "test-agent"));
    }

    private User sampleUser() {
        return User.builder().id(UUID.randomUUID()).email("investor@example.com")
                .riskProfile(RiskProfile.MODERATE).build();
    }

    @Test
    void getQuestions_shouldReturnAllCatalogQuestions() {
        assertThat(riskService.getQuestions()).hasSize(new RiskQuestionCatalog().getQuestions().size());
    }

    @Test
    void submit_shouldScoreAndPersistAssessmentAndUpdateUserRiskProfile() {
        User user = sampleUser();
        RiskSubmissionRequest request = new RiskSubmissionRequest(List.of(
                new RiskAnswerRequest("AGE", "UNDER_30"),
                new RiskAnswerRequest("RISK_APPETITE", "VERY_HIGH")));

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(riskAssessmentResultRepository.save(any(RiskAssessmentResult.class)))
                .thenAnswer(invocation -> {
                    RiskAssessmentResult result = invocation.getArgument(0);
                    result.setId(UUID.randomUUID());
                    result.setCreatedAt(Instant.now());
                    return result;
                });
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = riskService.submit(user.getEmail(), request);

        assertThat(response.score()).isEqualTo(100);
        assertThat(response.riskLevel()).isEqualTo(RiskLevel.AGGRESSIVE);
        assertThat(user.getRiskProfile()).isEqualTo(RiskProfile.AGGRESSIVE);
    }

    @Test
    void submit_shouldThrowWhenUserNotFound() {
        RiskSubmissionRequest request = new RiskSubmissionRequest(List.of(new RiskAnswerRequest("AGE", "UNDER_30")));
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskService.submit("missing@example.com", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getLatest_shouldThrowWhenNoAssessmentExists() {
        User user = sampleUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(riskAssessmentResultRepository.findFirstByUser_IdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskService.getLatest(user.getEmail()))
                .isInstanceOf(RiskAssessmentNotFoundException.class);
    }

    @Test
    void getLatest_shouldReturnMostRecentAssessment() {
        User user = sampleUser();
        RiskAssessmentResult result = RiskAssessmentResult.builder()
                .id(UUID.randomUUID())
                .user(user)
                .score(60)
                .riskLevel(RiskLevel.BALANCED)
                .recommendation(RiskRecommendation.builder().summary("Balanced").suggestedAllocation(java.util.Map.of())
                        .suggestedInvestmentHorizon("5 years").suggestedFundCategories(List.of("Balanced Advantage Funds")).build())
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(riskAssessmentResultRepository.findFirstByUser_IdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.of(result));

        var response = riskService.getLatest(user.getEmail());

        assertThat(response.score()).isEqualTo(60);
        assertThat(response.riskLevel()).isEqualTo(RiskLevel.BALANCED);
    }
}
