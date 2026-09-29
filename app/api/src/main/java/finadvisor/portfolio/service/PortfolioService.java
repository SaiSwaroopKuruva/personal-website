package finadvisor.portfolio.service;

import finadvisor.portfolio.dto.CreatePortfolioRequest;
import finadvisor.portfolio.dto.PortfolioResponse;
import finadvisor.portfolio.dto.UpdatePortfolioRequest;
import finadvisor.portfolio.entity.Portfolio;

import java.util.List;
import java.util.UUID;

public interface PortfolioService {

    List<PortfolioResponse> listPortfolios(String userEmail);

    PortfolioResponse createPortfolio(String userEmail, CreatePortfolioRequest request);

    PortfolioResponse getPortfolio(String userEmail, UUID portfolioId);

    PortfolioResponse updatePortfolio(String userEmail, UUID portfolioId, UpdatePortfolioRequest request);

    void deletePortfolio(String userEmail, UUID portfolioId);

    /** Loads the owned portfolio entity or throws {@link finadvisor.portfolio.exception.PortfolioNotFoundException} - never leaks another user's portfolio (Part 19). */
    Portfolio requireOwnedPortfolio(String userEmail, UUID portfolioId);
}
