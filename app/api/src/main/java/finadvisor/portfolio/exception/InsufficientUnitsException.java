package finadvisor.portfolio.exception;

/** A sale/redemption was rejected because it would exceed the units currently held (Part 5). */
public class InsufficientUnitsException extends RuntimeException {
    public InsufficientUnitsException(String message) {
        super(message);
    }
}
