package finadvisor.service;

import finadvisor.dto.calculator.SwpCalculatorRequest;
import finadvisor.dto.calculator.SwpCalculatorResponse;

public interface SwpCalculatorService {
    SwpCalculatorResponse calculate(SwpCalculatorRequest request);
}
