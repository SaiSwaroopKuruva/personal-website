package finadvisor.entity;

/** Supported trailing-return periods. {@link #code} is the compact form persisted in the database. */
public enum ReturnPeriod {
    ONE_DAY("1D"),
    ONE_WEEK("1W"),
    ONE_MONTH("1M"),
    THREE_MONTH("3M"),
    SIX_MONTH("6M"),
    ONE_YEAR("1Y"),
    THREE_YEAR("3Y"),
    FIVE_YEAR("5Y"),
    TEN_YEAR("10Y"),
    SINCE_INCEPTION("SINCE_INCEPTION");

    private final String code;

    ReturnPeriod(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ReturnPeriod fromCode(String code) {
        for (ReturnPeriod period : values()) {
            if (period.code.equals(code)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Unknown return period code: " + code);
    }
}
