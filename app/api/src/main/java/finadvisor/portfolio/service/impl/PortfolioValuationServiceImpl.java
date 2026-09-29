package finadvisor.portfolio.service.impl;

import finadvisor.marketdata.DataFreshness;
import finadvisor.mutualfund.entity.FundStatus;
import finadvisor.mutualfund.entity.MutualFund;
import finadvisor.mutualfund.repository.MutualFundRepository;
import finadvisor.portfolio.dto.ValuationInfo;
import finadvisor.portfolio.dto.ValuationStatus;
import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.service.HoldingPosition;
import finadvisor.portfolio.service.PortfolioValuationService;
import finadvisor.portfolio.service.ValuedHolding;
import finadvisor.stock.dto.StockDetailsResponse;
import finadvisor.stock.dto.StockQuoteResponse;
import finadvisor.stock.service.StockService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PortfolioValuationServiceImpl implements PortfolioValuationService {

    private static final Logger log = Logger.getLogger(PortfolioValuationServiceImpl.class.getName());
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final int MUTUAL_FUND_NAV_FRESH_DAYS = 3;

    private final StockService stockService;
    private final MutualFundRepository mutualFundRepository;

    public PortfolioValuationServiceImpl(StockService stockService, MutualFundRepository mutualFundRepository) {
        this.stockService = stockService;
        this.mutualFundRepository = mutualFundRepository;
    }

    @Override
    public List<ValuedHolding> valueHoldings(List<HoldingPosition> positions) {
        List<ValuedHolding> valued = new ArrayList<>();
        for (HoldingPosition position : positions) {
            valued.add(position.assetType() == AssetType.STOCK ? valueStock(position) : valueMutualFund(position));
        }
        return valued;
    }

    private ValuedHolding valueStock(HoldingPosition position) {
        String name = position.symbol();
        try {
            StockDetailsResponse details = stockService.getDetails(position.symbol());
            name = details.companyName();
        } catch (RuntimeException ex) {
            log.log(Level.FINE, "Could not resolve stock details for " + position.symbol(), ex);
        }

        try {
            StockQuoteResponse quote = stockService.getCurrentPrice(position.symbol());
            ValuationStatus status = mapStockFreshness(quote.freshness());
            ValuationInfo valuation = new ValuationInfo(quote.lastTradedPrice(), quote.source(), quote.timestamp(), Instant.now(), status);
            return buildValuedHolding(position, name, valuation);
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "Could not price stock " + position.symbol(), ex);
            return buildValuedHolding(position, name, ValuationInfo.unavailable());
        }
    }

    private ValuedHolding valueMutualFund(HoldingPosition position) {
        return mutualFundRepository.findBySchemeCode(position.symbol())
                .map(fund -> {
                    String name = fund.getSchemeName();
                    if (fund.getNav() == null || fund.getNavDate() == null || fund.getStatus() == FundStatus.INACTIVE) {
                        return buildValuedHolding(position, name, ValuationInfo.unavailable());
                    }
                    ValuationStatus status = isNavFresh(fund.getNavDate()) ? ValuationStatus.CURRENT : ValuationStatus.STALE;
                    Instant navTimestamp = fund.getNavDate().atStartOfDay(ZoneOffset.UTC).toInstant();
                    ValuationInfo valuation = new ValuationInfo(fund.getNav(), "AMFI", navTimestamp, Instant.now(), status);
                    return buildValuedHolding(position, name, valuation);
                })
                .orElseGet(() -> {
                    log.log(Level.WARNING, "Could not resolve mutual fund scheme " + position.symbol());
                    return buildValuedHolding(position, position.symbol(), ValuationInfo.unavailable());
                });
    }

    private ValuedHolding buildValuedHolding(HoldingPosition position, String name, ValuationInfo valuation) {
        if (valuation.status() == ValuationStatus.UNAVAILABLE || valuation.price() == null) {
            return new ValuedHolding(position, name, valuation, null, null, null);
        }
        BigDecimal marketValue = position.quantity().multiply(valuation.price(), MC).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedGain = marketValue.subtract(position.costBasis());
        BigDecimal unrealizedGainPercent = position.costBasis().signum() > 0
                ? unrealizedGain.divide(position.costBasis(), MC).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : null;
        return new ValuedHolding(position, name, valuation, marketValue, unrealizedGain.setScale(2, RoundingMode.HALF_UP), unrealizedGainPercent);
    }

    private ValuationStatus mapStockFreshness(DataFreshness freshness) {
        return switch (freshness) {
            case LIVE, FRESH -> ValuationStatus.CURRENT;
            case STALE, UNKNOWN -> ValuationStatus.STALE;
        };
    }

    private boolean isNavFresh(LocalDate navDate) {
        return !navDate.isBefore(LocalDate.now().minusDays(MUTUAL_FUND_NAV_FRESH_DAYS));
    }
}
