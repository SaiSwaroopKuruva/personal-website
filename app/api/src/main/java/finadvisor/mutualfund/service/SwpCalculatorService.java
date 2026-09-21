package finadvisor.mutualfund.service;

import finadvisor.mutualfund.dto.calculator.SwpCalculatorRequest;
import finadvisor.mutualfund.dto.calculator.SwpCalculatorResponse;

public interface SwpCalculatorService {
    SwpCalculatorResponse calculate(SwpCalculatorRequest request);
}
