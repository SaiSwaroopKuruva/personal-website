package finadvisor.mutualfund.service;

import finadvisor.mutualfund.dto.calculator.SipCalculatorRequest;
import finadvisor.mutualfund.dto.calculator.SipCalculatorResponse;

public interface SipCalculatorService {
    SipCalculatorResponse calculate(SipCalculatorRequest request);
}
