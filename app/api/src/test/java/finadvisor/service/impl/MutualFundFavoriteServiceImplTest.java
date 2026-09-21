package finadvisor.service.impl;

import finadvisor.entity.FundStatus;
import finadvisor.entity.MutualFund;
import finadvisor.entity.MutualFundAmc;
import finadvisor.entity.OptionType;
import finadvisor.entity.PlanType;
import finadvisor.entity.User;
import finadvisor.entity.UserMutualFundFavorite;
import finadvisor.exception.DuplicateFavoriteException;
import finadvisor.exception.MutualFundNotFoundException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mapper.MutualFundMapper;
import finadvisor.repository.MutualFundRepository;
import finadvisor.repository.MutualFundReturnRepository;
import finadvisor.repository.UserMutualFundFavoriteRepository;
import finadvisor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MutualFundFavoriteServiceImplTest {

    @Mock
    private UserMutualFundFavoriteRepository favoriteRepository;

    @Mock
    private MutualFundRepository mutualFundRepository;

    @Mock
    private MutualFundReturnRepository returnRepository;

    @Mock
    private UserRepository userRepository;

    private MutualFundFavoriteServiceImpl service;

    private User user;
    private MutualFund fund;

    @BeforeEach
    void setUp() {
        service = new MutualFundFavoriteServiceImpl(favoriteRepository, mutualFundRepository, returnRepository,
                userRepository, new MutualFundMapper());
        user = User.builder().id(UUID.randomUUID()).email("investor@example.com").build();
        fund = MutualFund.builder().id(UUID.randomUUID()).schemeCode("DEMO001")
                .amc(MutualFundAmc.builder().code("NILGIRI").name("Nilgiri Mutual Fund").build())
                .schemeName("Nilgiri Bluechip Equity Fund").planType(PlanType.DIRECT).optionType(OptionType.GROWTH)
                .status(FundStatus.ACTIVE).build();
    }

    @Test
    void addFavorite_shouldSaveWhenNotAlreadyFavorited() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(mutualFundRepository.findBySchemeCodeAndStatusNot("DEMO001", FundStatus.INACTIVE)).thenReturn(Optional.of(fund));
        when(favoriteRepository.existsByUser_IdAndMutualFund_Id(user.getId(), fund.getId())).thenReturn(false);

        var response = service.addFavorite(user.getEmail(), "DEMO001");

        assertThat(response.favorite()).isTrue();
        verify(favoriteRepository, times(1)).save(any(UserMutualFundFavorite.class));
    }

    @Test
    void addFavorite_shouldRejectDuplicate() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(mutualFundRepository.findBySchemeCodeAndStatusNot("DEMO001", FundStatus.INACTIVE)).thenReturn(Optional.of(fund));
        when(favoriteRepository.existsByUser_IdAndMutualFund_Id(user.getId(), fund.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.addFavorite(user.getEmail(), "DEMO001"))
                .isInstanceOf(DuplicateFavoriteException.class);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_shouldThrowWhenFundNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(mutualFundRepository.findBySchemeCodeAndStatusNot("MISSING", FundStatus.INACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addFavorite(user.getEmail(), "MISSING"))
                .isInstanceOf(MutualFundNotFoundException.class);
    }

    @Test
    void addFavorite_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addFavorite("missing@example.com", "DEMO001"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void removeFavorite_shouldDeleteAndReturnFalseStatus() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(mutualFundRepository.findBySchemeCodeAndStatusNot("DEMO001", FundStatus.INACTIVE)).thenReturn(Optional.of(fund));

        var response = service.removeFavorite(user.getEmail(), "DEMO001");

        assertThat(response.favorite()).isFalse();
        verify(favoriteRepository).deleteByUser_IdAndMutualFund_Id(user.getId(), fund.getId());
    }
}
