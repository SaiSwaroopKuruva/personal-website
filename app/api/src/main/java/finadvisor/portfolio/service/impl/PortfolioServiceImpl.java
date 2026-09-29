package finadvisor.portfolio.service.impl;

import finadvisor.entity.User;
import finadvisor.exception.UserNotFoundException;
import finadvisor.repository.UserRepository;
import finadvisor.portfolio.dto.CreatePortfolioRequest;
import finadvisor.portfolio.dto.PortfolioResponse;
import finadvisor.portfolio.dto.UpdatePortfolioRequest;
import finadvisor.portfolio.entity.Portfolio;
import finadvisor.portfolio.exception.DuplicatePortfolioNameException;
import finadvisor.portfolio.exception.PortfolioNotFoundException;
import finadvisor.portfolio.repository.PortfolioRepository;
import finadvisor.portfolio.service.PortfolioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioServiceImpl(PortfolioRepository portfolioRepository, UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponse> listPortfolios(String userEmail) {
        User user = findUser(userEmail);
        return portfolioRepository.findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PortfolioResponse createPortfolio(String userEmail, CreatePortfolioRequest request) {
        User user = findUser(userEmail);
        if (portfolioRepository.existsByUserIdAndNameIgnoreCase(user.getId(), request.name())) {
            throw new DuplicatePortfolioNameException("A portfolio named '" + request.name() + "' already exists");
        }
        boolean makeDefault = Boolean.TRUE.equals(request.isDefault()) || !portfolioRepository.existsByUserIdAndIsDefaultTrue(user.getId());
        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .baseCurrency("INR")
                .isDefault(makeDefault)
                .build();
        if (makeDefault) {
            clearExistingDefault(user.getId());
        }
        return toResponse(portfolioRepository.save(portfolio));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(String userEmail, UUID portfolioId) {
        return toResponse(requireOwnedPortfolio(userEmail, portfolioId));
    }

    @Override
    public PortfolioResponse updatePortfolio(String userEmail, UUID portfolioId, UpdatePortfolioRequest request) {
        Portfolio portfolio = requireOwnedPortfolio(userEmail, portfolioId);
        if (!portfolio.getName().equalsIgnoreCase(request.name())
                && portfolioRepository.existsByUserIdAndNameIgnoreCase(portfolio.getUser().getId(), request.name())) {
            throw new DuplicatePortfolioNameException("A portfolio named '" + request.name() + "' already exists");
        }
        portfolio.setName(request.name());
        portfolio.setDescription(request.description());
        if (Boolean.TRUE.equals(request.isDefault()) && !portfolio.isDefault()) {
            clearExistingDefault(portfolio.getUser().getId());
            portfolio.setDefault(true);
        }
        return toResponse(portfolioRepository.save(portfolio));
    }

    @Override
    public void deletePortfolio(String userEmail, UUID portfolioId) {
        Portfolio portfolio = requireOwnedPortfolio(userEmail, portfolioId);
        portfolio.setArchivedAt(java.time.Instant.now());
        portfolioRepository.save(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public Portfolio requireOwnedPortfolio(String userEmail, UUID portfolioId) {
        User user = findUser(userEmail);
        return portfolioRepository.findByIdAndUserId(portfolioId, user.getId())
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));
    }

    private void clearExistingDefault(UUID userId) {
        portfolioRepository.findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(userId).stream()
                .filter(Portfolio::isDefault)
                .forEach(p -> {
                    p.setDefault(false);
                    portfolioRepository.save(p);
                });
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(), portfolio.getName(), portfolio.getDescription(), portfolio.getBaseCurrency(),
                portfolio.isDefault(), portfolio.getCreatedAt(), portfolio.getUpdatedAt(), portfolio.getArchivedAt());
    }
}
