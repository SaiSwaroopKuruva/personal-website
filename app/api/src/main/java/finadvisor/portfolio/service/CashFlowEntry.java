package finadvisor.portfolio.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One dated investor cash flow for XIRR (Part 12). Sign convention: negative = money the investor put in
 * (BUY/PURCHASE/FEE/TAX), positive = money the investor received (SELL/REDEMPTION/DIVIDEND), or the
 * positive terminal portfolio value on the valuation date. ADJUSTMENT rows never appear here (non-cash).
 */
public record CashFlowEntry(LocalDate date, BigDecimal amount) {
}
