package finadvisor.portfolio.util;

/** Shared CSV cell escaping/sanitization for all portfolio exports (Part 17). */
public final class CsvUtil {

    private CsvUtil() {
    }

    /** Quotes/escapes a cell per RFC 4180 and neutralizes spreadsheet formula injection. */
    public static String cell(Object value) {
        String raw = value == null ? "" : String.valueOf(value);
        String sanitized = sanitizeFormulaInjection(raw);
        if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n") || sanitized.contains("\r")) {
            return "\"" + sanitized.replace("\"", "\"\"") + "\"";
        }
        return sanitized;
    }

    /** Prefixes values that would otherwise be interpreted as a formula by Excel/Sheets/LibreOffice. */
    private static String sanitizeFormulaInjection(String value) {
        if (!value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0)) >= 0) {
            return "'" + value;
        }
        return value;
    }

    public static String row(Object... cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(cell(cells[i]));
        }
        sb.append("\r\n");
        return sb.toString();
    }
}
