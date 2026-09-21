package finadvisor.service;

import finadvisor.dto.calculator.LumpsumCalculatorRequest;
import finadvisor.dto.calculator.LumpsumCalculatorResponse;

public interface LumpsumCalculatorService {
    LumpsumCalculatorResponse calculate(LumpsumCalculatorRequest request);
}
