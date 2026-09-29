package finadvisor.portfolio.service;

import finadvisor.portfolio.dto.AllocationResponse;
import finadvisor.portfolio.dto.PerformanceResponse;
import finadvisor.portfolio.dto.PortfolioHoldingsResponse;
import finadvisor.portfolio.dto.PortfolioSummaryResponse;
import finadvisor.portfolio.dto.XirrResponse;

import java.util.UUID;

public interface PortfolioAnalyticsService {

    PortfolioHoldingsResponse getHoldings(String userEmail, UUID portfolioId);

    PortfolioSummaryResponse getSummary(String userEmail, UUID portfolioId);

    AllocationResponse getAllocation(String userEmail, UUID portfolioId);

    PerformanceResponse getPerformance(String userEmail, UUID portfolioId, String range);

    XirrResponse getXirr(String userEmail, UUID portfolioId);
}
