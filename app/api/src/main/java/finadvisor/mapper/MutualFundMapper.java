package finadvisor.mapper;

import finadvisor.dto.mutualfund.ComparisonFundResponse;
import finadvisor.dto.mutualfund.FundManagerResponse;
import finadvisor.dto.mutualfund.HoldingResponse;
import finadvisor.dto.mutualfund.MutualFundDetailsResponse;
import finadvisor.dto.mutualfund.MutualFundSummaryResponse;
import finadvisor.dto.mutualfund.ReturnResponse;
import finadvisor.entity.MutualFund;
import finadvisor.entity.MutualFundHolding;
import finadvisor.entity.MutualFundManager;
import finadvisor.entity.MutualFundReturn;
import finadvisor.entity.ReturnPeriod;
import finadvisor.util.FinancialDisclaimers;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class MutualFundMapper {

    public MutualFundSummaryResponse toSummary(MutualFund fund, Map<ReturnPeriod, BigDecimal> returnsByPeriod, boolean favorite) {
        return new MutualFundSummaryResponse(
                fund.getSchemeCode(),
                fund.getIsin(),
                fund.getSchemeName(),
                fund.getShortName(),
                fund.getAmc().getCode(),
                fund.getAmc().getName(),
                fund.getCategory(),
                fund.getSubCategory(),
                fund.getPlanType().name(),
                fund.getOptionType().name(),
                fund.getRiskLevel().name(),
                fund.getNav(),
                fund.getNavDate(),
                fund.getExpenseRatio(),
                fund.getAum(),
                fund.getMinimumSip(),
                fund.getMinimumLumpsum(),
                returnsByPeriod.get(ReturnPeriod.ONE_YEAR),
                returnsByPeriod.get(ReturnPeriod.THREE_YEAR),
                returnsByPeriod.get(ReturnPeriod.FIVE_YEAR),
                favorite);
    }

    public MutualFundDetailsResponse toDetails(MutualFund fund, List<MutualFundReturn> returns, List<MutualFundManager> managers,
                                                List<MutualFundHolding> topHoldings, LocalDate holdingsAsOfDate, boolean favorite) {
        return new MutualFundDetailsResponse(
                fund.getSchemeCode(),
                fund.getIsin(),
                fund.getSchemeName(),
                fund.getShortName(),
                fund.getAmc().getCode(),
                fund.getAmc().getName(),
                fund.getCategory(),
                fund.getSubCategory(),
                fund.getPlanType().name(),
                fund.getOptionType().name(),
                fund.getAssetClass().name(),
                fund.getInvestmentObjective(),
                fund.getRiskLevel().name(),
                fund.getBenchmark(),
                fund.getExpenseRatio(),
                fund.getExitLoad(),
                fund.getMinimumLumpsum(),
                fund.getMinimumSip(),
                fund.getAum(),
                fund.getNav(),
                fund.getNavDate(),
                fund.getInceptionDate(),
                fund.getFundManager(),
                managers.stream().map(this::toManager).toList(),
                returns.stream().map(this::toReturn).toList(),
                topHoldings.stream().map(this::toHolding).toList(),
                holdingsAsOfDate,
                favorite,
                FinancialDisclaimers.MUTUAL_FUND_GENERAL);
    }

    public HoldingResponse toHolding(MutualFundHolding holding) {
        return new HoldingResponse(
                holding.getSecurityName(),
                holding.getIsin(),
                holding.getSector(),
                holding.getAssetType().name(),
                holding.getWeightPercentage(),
                holding.getQuantity(),
                holding.getMarketValue(),
                holding.getAsOfDate());
    }

    public FundManagerResponse toManager(MutualFundManager manager) {
        return new FundManagerResponse(
                manager.getName(),
                manager.getDesignation(),
                manager.getExperienceYears(),
                manager.getJoiningDate(),
                manager.getBio(),
                manager.isActive());
    }

    public ReturnResponse toReturn(MutualFundReturn mutualFundReturn) {
        return new ReturnResponse(
                mutualFundReturn.getReturnPeriod().code(),
                mutualFundReturn.getReturnPercentage(),
                mutualFundReturn.isAnnualized(),
                mutualFundReturn.getCalculatedAsOf());
    }

    public ComparisonFundResponse toComparison(MutualFund fund, Map<ReturnPeriod, BigDecimal> returnsByPeriod) {
        return new ComparisonFundResponse(
                fund.getSchemeCode(),
                fund.getSchemeName(),
                fund.getAmc().getName(),
                fund.getCategory(),
                fund.getSubCategory(),
                fund.getRiskLevel().name(),
                fund.getNav(),
                fund.getAum(),
                fund.getExpenseRatio(),
                returnsByPeriod.get(ReturnPeriod.ONE_YEAR),
                returnsByPeriod.get(ReturnPeriod.THREE_YEAR),
                returnsByPeriod.get(ReturnPeriod.FIVE_YEAR),
                returnsByPeriod.get(ReturnPeriod.SINCE_INCEPTION),
                fund.getMinimumSip(),
                fund.getMinimumLumpsum(),
                fund.getExitLoad(),
                fund.getBenchmark(),
                fund.getFundManager());
    }
}
