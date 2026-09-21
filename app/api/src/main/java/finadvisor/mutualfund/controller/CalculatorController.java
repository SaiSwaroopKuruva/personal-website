package finadvisor.mutualfund.controller;

import finadvisor.mutualfund.dto.calculator.LumpsumCalculatorRequest;
import finadvisor.mutualfund.dto.calculator.LumpsumCalculatorResponse;
import finadvisor.mutualfund.dto.calculator.SipCalculatorRequest;
import finadvisor.mutualfund.dto.calculator.SipCalculatorResponse;
import finadvisor.mutualfund.dto.calculator.SwpCalculatorRequest;
import finadvisor.mutualfund.dto.calculator.SwpCalculatorResponse;
import finadvisor.mutualfund.service.LumpsumCalculatorService;
import finadvisor.mutualfund.service.SipCalculatorService;
import finadvisor.mutualfund.service.SwpCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculators")
@RequiredArgsConstructor
@Tag(name = "Investment Calculators", description = "SIP, lumpsum and SWP estimate calculators (educational only, not investment advice)")
public class CalculatorController {

    private final SipCalculatorService sipCalculatorService;
    private final LumpsumCalculatorService lumpsumCalculatorService;
    private final SwpCalculatorService swpCalculatorService;

    @PostMapping("/sip")
    @Operation(summary = "Estimate SIP future value", description = "Public endpoint; assumes constant monthly compounding. Estimate only, not a guarantee.")
    public ResponseEntity<SipCalculatorResponse> sip(@Valid @RequestBody SipCalculatorRequest request) {
        return ResponseEntity.ok(sipCalculatorService.calculate(request));
    }

    @PostMapping("/lumpsum")
    @Operation(summary = "Estimate lumpsum future value", description = "Public endpoint; assumes constant annual compounding. Estimate only, not a guarantee.")
    public ResponseEntity<LumpsumCalculatorResponse> lumpsum(@Valid @RequestBody LumpsumCalculatorRequest request) {
        return ResponseEntity.ok(lumpsumCalculatorService.calculate(request));
    }

    @PostMapping("/swp")
    @Operation(summary = "Estimate a Systematic Withdrawal Plan", description = "Public endpoint; simulates monthly growth/withdrawal and reports early exhaustion if applicable.")
    public ResponseEntity<SwpCalculatorResponse> swp(@Valid @RequestBody SwpCalculatorRequest request) {
        return ResponseEntity.ok(swpCalculatorService.calculate(request));
    }
}
