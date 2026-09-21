package finadvisor.service;

import finadvisor.dto.calculator.SipCalculatorRequest;
import finadvisor.dto.calculator.SipCalculatorResponse;

public interface SipCalculatorService {
    SipCalculatorResponse calculate(SipCalculatorRequest request);
}
