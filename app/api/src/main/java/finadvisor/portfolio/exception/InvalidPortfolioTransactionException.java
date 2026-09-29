package finadvisor.portfolio.exception;

/** Invalid transaction input: wrong type for the asset, bad identifiers, invalid amounts, etc. */
public class InvalidPortfolioTransactionException extends RuntimeException {
    public InvalidPortfolioTransactionException(String message) {
        super(message);
    }
}
