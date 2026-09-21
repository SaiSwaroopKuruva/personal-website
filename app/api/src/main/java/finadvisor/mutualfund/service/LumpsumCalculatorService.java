package finadvisor.mutualfund.service;

import finadvisor.mutualfund.dto.calculator.LumpsumCalculatorRequest;
import finadvisor.mutualfund.dto.calculator.LumpsumCalculatorResponse;

public interface LumpsumCalculatorService {
    LumpsumCalculatorResponse calculate(LumpsumCalculatorRequest request);
}
