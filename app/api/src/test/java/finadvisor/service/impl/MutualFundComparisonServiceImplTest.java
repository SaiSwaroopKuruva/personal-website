package finadvisor.service.impl;

import finadvisor.entity.FundRiskLevel;
import finadvisor.entity.FundStatus;
import finadvisor.entity.MutualFund;
import finadvisor.entity.MutualFundAmc;
import finadvisor.entity.OptionType;
import finadvisor.entity.PlanType;
import finadvisor.exception.InvalidComparisonRequestException;
import finadvisor.mapper.MutualFundMapper;
import finadvisor.repository.MutualFundRepository;
import finadvisor.repository.MutualFundReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MutualFundComparisonServiceImplTest {

    @Mock
    private MutualFundRepository mutualFundRepository;

    @Mock
    private MutualFundReturnRepository returnRepository;

    private MutualFundComparisonServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MutualFundComparisonServiceImpl(mutualFundRepository, returnRepository, new MutualFundMapper());
    }

    private MutualFund fund(String schemeCode) {
        return MutualFund.builder().id(UUID.randomUUID()).schemeCode(schemeCode)
                .amc(MutualFundAmc.builder().code("NILGIRI").name("Nilgiri Mutual Fund").build())
                .schemeName(schemeCode + " Fund").category("Equity").riskLevel(FundRiskLevel.VERY_HIGH)
                .planType(PlanType.DIRECT).optionType(OptionType.GROWTH).nav(new BigDecimal("15.00"))
                .status(FundStatus.ACTIVE).build();
    }

    @Test
    void compare_shouldReturnFundsInRequestedOrder() {
        MutualFund a = fund("DEMO001");
        MutualFund b = fund("DEMO002");
        when(mutualFundRepository.findBySchemeCodeInAndStatusNot(List.of("DEMO002", "DEMO001"), FundStatus.INACTIVE))
                .thenReturn(List.of(a, b));
        when(returnRepository.findByMutualFund_IdIn(List.of(a.getId(), b.getId()))).thenReturn(List.of());

        var response = service.compare(List.of("DEMO002", "DEMO001"));

        assertThat(response.funds()).extracting("schemeCode").containsExactly("DEMO002", "DEMO001");
        assertThat(response.disclaimer()).isNotBlank();
    }

    @Test
    void compare_shouldRejectFewerThanTwoFunds() {
        assertThatThrownBy(() -> service.compare(List.of("DEMO001")))
                .isInstanceOf(InvalidComparisonRequestException.class);
    }

    @Test
    void compare_shouldRejectMoreThanFourFunds() {
        assertThatThrownBy(() -> service.compare(List.of("A", "B", "C", "D", "E")))
                .isInstanceOf(InvalidComparisonRequestException.class);
    }

    @Test
    void compare_shouldRejectUnknownSchemeCode() {
        when(mutualFundRepository.findBySchemeCodeInAndStatusNot(List.of("DEMO001", "MISSING"), FundStatus.INACTIVE))
                .thenReturn(List.of(fund("DEMO001")));

        assertThatThrownBy(() -> service.compare(List.of("DEMO001", "MISSING")))
                .isInstanceOf(InvalidComparisonRequestException.class);
    }
}
