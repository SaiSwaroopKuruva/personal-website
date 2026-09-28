package finadvisor.stock.exception;

public class InvalidStockComparisonRequestException extends RuntimeException {
    public InvalidStockComparisonRequestException(String message) {
        super(message);
    }
}
