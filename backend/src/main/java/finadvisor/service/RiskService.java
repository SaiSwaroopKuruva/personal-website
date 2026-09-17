package finadvisor.service;

import finadvisor.dto.PageResponse;
import finadvisor.dto.risk.RiskAssessmentResponse;
import finadvisor.dto.risk.RiskQuestionResponse;
import finadvisor.dto.risk.RiskSubmissionRequest;

import java.util.List;

public interface RiskService {
    List<RiskQuestionResponse> getQuestions();

    RiskAssessmentResponse submit(String email, RiskSubmissionRequest request);

    RiskAssessmentResponse getLatest(String email);

    PageResponse<RiskAssessmentResponse> getHistory(String email, int page, int size);
}
