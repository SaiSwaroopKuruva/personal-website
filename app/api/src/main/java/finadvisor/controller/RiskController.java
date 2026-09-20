package finadvisor.controller;

import finadvisor.dto.PageResponse;
import finadvisor.dto.risk.RiskAssessmentResponse;
import finadvisor.dto.risk.RiskQuestionResponse;
import finadvisor.dto.risk.RiskSubmissionRequest;
import finadvisor.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
@Tag(name = "Risk Assessment", description = "Investor risk questionnaire and scoring engine")
public class RiskController {

    private final RiskService riskService;

    @GetMapping("/questions")
    @Operation(summary = "Get the configurable investor risk questionnaire")
    public ResponseEntity<List<RiskQuestionResponse>> getQuestions() {
        return ResponseEntity.ok(riskService.getQuestions());
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit questionnaire answers and receive a risk assessment")
    public ResponseEntity<RiskAssessmentResponse> submit(Authentication authentication,
                                                          @Valid @RequestBody RiskSubmissionRequest request) {
        return ResponseEntity.ok(riskService.submit(authentication.getName(), request));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get the most recent risk assessment result")
    public ResponseEntity<RiskAssessmentResponse> getLatest(Authentication authentication) {
        return ResponseEntity.ok(riskService.getLatest(authentication.getName()));
    }

    @GetMapping("/history")
    @Operation(summary = "Get a paginated history of past risk assessments")
    public ResponseEntity<PageResponse<RiskAssessmentResponse>> getHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(riskService.getHistory(authentication.getName(), page, size));
    }
}
