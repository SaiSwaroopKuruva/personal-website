package finadvisor.mutualfund.provider.amfi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses AMFI's published "NAVAll" text file. This is a plain-text, semicolon-separated snapshot with no
 * consistent header per row - schemes are grouped under repeating "category" lines
 * (e.g. {@code Open Ended Schemes(Debt Scheme - Overnight Fund)}) and, within each category, "AMC name"
 * lines (e.g. {@code Axis Mutual Fund}), separated by blank lines. This parser is defensive: malformed or
 * unexpected rows are skipped and counted rather than failing the whole sync (Part 14 - data validation).
 */
public final class AmfiNavParser {

    private static final Logger log = Logger.getLogger(AmfiNavParser.class.getName());
    private static final DateTimeFormatter NAV_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private AmfiNavParser() {
    }

    public record ParseResult(List<AmfiSchemeRecord> records, int skippedLines) {
    }

    public static ParseResult parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParseResult(List.of(), 0);
        }
        List<AmfiSchemeRecord> records = new ArrayList<>();
        int skipped = 0;
        String currentAmcName = null;
        String currentCategoryLine = null;

        for (String rawLine : rawText.split("\r?\n")) {
            String line = rawLine.strip();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("Scheme Code")) {
                continue; // column header row, repeats throughout the file
            }
            long semicolons = line.chars().filter(c -> c == ';').count();
            if (semicolons >= 5) {
                AmfiSchemeRecord record = parseDataRow(line, currentAmcName, currentCategoryLine);
                if (record == null) {
                    skipped++;
                } else {
                    records.add(record);
                }
                continue;
            }
            if (isCategoryHeader(line)) {
                currentCategoryLine = line;
            } else {
                // Any other bare (no ';') non-empty line is an AMC name heading.
                currentAmcName = line;
            }
        }
        return new ParseResult(records, skipped);
    }

    private static boolean isCategoryHeader(String line) {
        return line.contains("Schemes(") || (line.contains("(") && line.endsWith(")") && !line.contains(";"));
    }

    private static AmfiSchemeRecord parseDataRow(String line, String amcName, String categoryLine) {
        String[] fields = line.split(";", -1);
        if (fields.length < 6) {
            return null;
        }
        String schemeCode = fields[0].strip();
        String isinGrowth = blankToNull(fields[1].strip());
        String isinReinvestment = blankToNull(fields[2].strip());
        String schemeName = fields[3].strip();
        String navRaw = fields[4].strip();
        String dateRaw = fields[5].strip();

        if (schemeCode.isEmpty() || schemeName.isEmpty()) {
            return null;
        }
        BigDecimal nav = parseNav(navRaw);
        LocalDate navDate = parseDate(dateRaw);
        if (nav == null || navDate == null || amcName == null) {
            log.log(Level.FINE, () -> "Skipping AMFI row with invalid NAV/date/AMC context: " + line);
            return null;
        }
        return new AmfiSchemeRecord(schemeCode, isinGrowth, isinReinvestment, schemeName, nav, navDate, amcName, categoryLine);
    }

    private static BigDecimal parseNav(String raw) {
        if (raw.isEmpty() || raw.equalsIgnoreCase("N.A.") || raw.equals("-")) {
            return null;
        }
        try {
            BigDecimal nav = new BigDecimal(raw);
            // Reject negative/zero NAV outright (Part 14 - data validation): never a legitimate published NAV.
            return nav.signum() > 0 ? nav : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static LocalDate parseDate(String raw) {
        if (raw.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(raw, NAV_DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String blankToNull(String value) {
        if (value.isEmpty() || value.equals("-")) {
            return null;
        }
        return value;
    }
}
