package finadvisor.portfolio.entity;

/** Union of stock and mutual-fund transaction types (Part 4.3); validity per {@link AssetType} is enforced in the service layer. */
public enum TransactionType {
    BUY,
    SELL,
    PURCHASE,
    REDEMPTION,
    DIVIDEND,
    FEE,
    TAX,
    ADJUSTMENT
}
