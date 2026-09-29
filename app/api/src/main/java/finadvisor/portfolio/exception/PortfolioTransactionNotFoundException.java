package finadvisor.portfolio.exception;

public class PortfolioTransactionNotFoundException extends RuntimeException {
    public PortfolioTransactionNotFoundException(String message) {
        super(message);
    }
}
