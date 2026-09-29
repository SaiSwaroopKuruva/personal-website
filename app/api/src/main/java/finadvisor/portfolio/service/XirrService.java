package finadvisor.portfolio.service;

import finadvisor.portfolio.dto.XirrResponse;

import java.util.List;

public interface XirrService {

    /**
     * Computes the annualized, money-weighted return from dated investor cash flows (Part 12).
     * {@code cashFlows} must already include the terminal portfolio value as a positive entry on the
     * valuation date - this service does not add it implicitly.
     */
    XirrResponse calculate(List<CashFlowEntry> cashFlows);
}
