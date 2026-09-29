package finadvisor.portfolio.service.impl;

import finadvisor.entity.User;
import finadvisor.repository.UserRepository;
import finadvisor.portfolio.entity.Portfolio;
import finadvisor.portfolio.exception.DuplicatePortfolioNameException;
import finadvisor.portfolio.exception.PortfolioNotFoundException;
import finadvisor.portfolio.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplTest {

    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private UserRepository userRepository;

    private PortfolioServiceImpl service;

    private final User owner = User.builder().id(UUID.randomUUID()).email("owner@example.com").build();
    private final User intruder = User.builder().id(UUID.randomUUID()).email("intruder@example.com").build();

    @BeforeEach
    void setUp() {
        service = new PortfolioServiceImpl(portfolioRepository, userRepository);
    }

    @Test
    void requireOwnedPortfolio_anotherUsersPortfolio_throwsNotFound() {
        UUID portfolioId = UUID.randomUUID();
        when(userRepository.findByEmail("intruder@example.com")).thenReturn(Optional.of(intruder));
        // Simulates the repository query being scoped by (id, userId): an intruder never gets a match.
        when(portfolioRepository.findByIdAndUserId(portfolioId, intruder.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireOwnedPortfolio("intruder@example.com", portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class);
    }

    @Test
    void requireOwnedPortfolio_ownersPortfolio_isReturned() {
        UUID portfolioId = UUID.randomUUID();
        Portfolio portfolio = Portfolio.builder().id(portfolioId).user(owner).name("Retirement").build();
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(portfolioRepository.findByIdAndUserId(portfolioId, owner.getId())).thenReturn(Optional.of(portfolio));

        Portfolio result = service.requireOwnedPortfolio("owner@example.com", portfolioId);

        assertThat(result.getId()).isEqualTo(portfolioId);
    }

    @Test
    void createPortfolio_duplicateNameForSameUser_throws() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(portfolioRepository.existsByUserIdAndNameIgnoreCase(owner.getId(), "Retirement")).thenReturn(true);

        var request = new finadvisor.portfolio.dto.CreatePortfolioRequest("Retirement", null, null);

        assertThatThrownBy(() -> service.createPortfolio("owner@example.com", request))
                .isInstanceOf(DuplicatePortfolioNameException.class);
    }

    @Test
    void listPortfolios_onlyReturnsRequestingUsersPortfolios() {
        Portfolio ownerPortfolio = Portfolio.builder().id(UUID.randomUUID()).user(owner).name("Growth").baseCurrency("INR").build();
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(portfolioRepository.findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(owner.getId()))
                .thenReturn(List.of(ownerPortfolio));

        var result = service.listPortfolios("owner@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Growth");
    }
}
