package finadvisor.mutualfund.provider.amfi;

import finadvisor.marketdata.DataType;
import finadvisor.marketdata.ProviderHealthTracker;
import finadvisor.mutualfund.entity.AssetClass;
import finadvisor.mutualfund.entity.FundRiskLevel;
import finadvisor.mutualfund.entity.OptionType;
import finadvisor.mutualfund.entity.PlanType;
import finadvisor.mutualfund.provider.MutualFundDataProvider;
import finadvisor.mutualfund.provider.ProviderException;
import finadvisor.mutualfund.provider.ProviderFundData;
import finadvisor.mutualfund.provider.ProviderHoldingData;
import finadvisor.mutualfund.provider.ProviderManagerData;
import finadvisor.mutualfund.provider.ProviderNavPoint;
import finadvisor.mutualfund.provider.ProviderResponse;
import finadvisor.mutualfund.provider.ProviderReturnData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Real mutual-fund NAV provider backed by AMFI's official published daily NAV file (Part 11). Activate with
 * {@code MUTUAL_FUND_PROVIDER_NAME=amfi}. AMFI publishes only scheme master data + latest NAV per scheme -
 * it does NOT publish holdings, fund managers, trailing returns, or a per-scheme historical NAV REST API, so
 * those methods return a clearly labeled "unsupported by this provider" failure rather than fabricating data
 * (Part 37/39). See docs/financial-data-providers.md for the full limitation writeup.
 */
@Component
@ConditionalOnProperty(prefix = "mutualfund.provider", name = "name", havingValue = "amfi")
public class AmfiMutualFundDataProvider implements MutualFundDataProvider {

    private static final Logger log = Logger.getLogger(AmfiMutualFundDataProvider.class.getName());
    private static final String PROVIDER_NAME = "AMFI";
    private static final String HEALTH_KEY = "mutualFunds";

    private final RestClient restClient;
    private final AmfiProperties properties;
    private final ProviderHealthTracker healthTracker;
    private final ReentrantLock refreshLock = new ReentrantLock();

    private volatile List<AmfiSchemeRecord> cachedRecords = List.of();
    private volatile Instant cachedAt = Instant.EPOCH;

    public AmfiMutualFundDataProvider(RestClient.Builder restClientBuilder, AmfiProperties properties,
                                       ProviderHealthTracker healthTracker) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
        this.healthTracker = healthTracker;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public ProviderResponse<List<ProviderFundData>> searchFunds(String query) {
        String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        List<ProviderFundData> matches = snapshot().stream()
                .filter(r -> needle.isEmpty() || r.schemeName().toLowerCase(Locale.ROOT).contains(needle))
                .map(this::toFundData)
                .toList();
        return ProviderResponse.ok(matches, PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<ProviderFundData> getFundDetails(String schemeCode) {
        return findByCode(schemeCode)
                .map(r -> ProviderResponse.ok(toFundData(r), PROVIDER_NAME))
                .orElseGet(() -> ProviderResponse.failure("Unknown AMFI scheme code: " + schemeCode, PROVIDER_NAME));
    }

    @Override
    public ProviderResponse<ProviderNavPoint> getCurrentNav(String schemeCode) {
        return findByCode(schemeCode)
                .map(r -> ProviderResponse.ok(new ProviderNavPoint(r.schemeCode(), r.nav(), r.navDate()), PROVIDER_NAME))
                .orElseGet(() -> ProviderResponse.failure("Unknown AMFI scheme code: " + schemeCode, PROVIDER_NAME));
    }

    @Override
    public ProviderResponse<List<ProviderNavPoint>> getHistoricalNav(String schemeCode, LocalDate from, LocalDate to) {
        // AMFI does not publish a documented per-scheme historical NAV REST API - only the current-day
        // NAVAll snapshot. Historical NAV in this platform is instead accumulated day-by-day into
        // mutual_fund_nav_history by the scheduled NAV sync job. See docs/financial-data-providers.md.
        return ProviderResponse.failure(
                "AMFI has no documented per-scheme historical NAV API; historical NAV is accumulated locally via daily sync, not fetched on demand.",
                PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<ProviderHoldingData>> getHoldings(String schemeCode) {
        return ProviderResponse.failure(
                "AMFI's published NAV data does not include portfolio holdings for any scheme (provider limitation).",
                PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<ProviderManagerData>> getFundManagers(String schemeCode) {
        return ProviderResponse.failure(
                "AMFI's published NAV data does not include fund manager information (provider limitation).",
                PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<ProviderReturnData>> getFundReturns(String schemeCode) {
        return ProviderResponse.failure(
                "AMFI's published NAV data does not include pre-calculated trailing returns (provider limitation).",
                PROVIDER_NAME);
    }

    @Override
    public ProviderResponse<List<ProviderFundData>> syncFunds() {
        List<AmfiSchemeRecord> records = fetchAndParse();
        return ProviderResponse.ok(records.stream().map(this::toFundData).toList(), PROVIDER_NAME);
    }

    /** Returns the cached snapshot, refreshing it first if it is missing or older than the configured TTL. */
    private List<AmfiSchemeRecord> snapshot() {
        if (cachedRecords.isEmpty() || Instant.now().isAfter(cachedAt.plus(properties.getCacheTtlMinutes(), ChronoUnit.MINUTES))) {
            return fetchAndParse();
        }
        return cachedRecords;
    }

    private Optional<AmfiSchemeRecord> findByCode(String schemeCode) {
        return snapshot().stream().filter(r -> r.schemeCode().equals(schemeCode)).findFirst();
    }

    private List<AmfiSchemeRecord> fetchAndParse() {
        refreshLock.lock();
        try {
            // Re-check under the lock: another thread may have just refreshed it.
            if (!cachedRecords.isEmpty() && Instant.now().isBefore(cachedAt.plus(properties.getCacheTtlMinutes(), ChronoUnit.MINUTES))) {
                return cachedRecords;
            }
            String rawText = fetchRawText();
            AmfiNavParser.ParseResult result = AmfiNavParser.parse(rawText);
            if (result.records().isEmpty()) {
                throw new ProviderException("AMFI NAV file parsed to zero valid records - treating as an invalid provider response", false);
            }
            log.info(() -> "provider=AMFI operation=FETCH_NAV_ALL status=SUCCESS records=" + result.records().size()
                    + " skipped=" + result.skippedLines());
            cachedRecords = result.records();
            cachedAt = Instant.now();
            healthTracker.recordSuccess(HEALTH_KEY, PROVIDER_NAME, DataType.LATEST_NAV);
            return cachedRecords;
        } finally {
            refreshLock.unlock();
        }
    }

    private String fetchRawText() {
        try {
            String body = restClient.get()
                    .uri(properties.getNavAllUrl())
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) {
                throw new ProviderException("AMFI NAV file response was empty", true);
            }
            return body;
        } catch (RestClientResponseException ex) {
            boolean retryable = ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
            log.log(Level.WARNING, () -> "provider=AMFI operation=FETCH_NAV_ALL status=FAILED httpStatus=" + ex.getStatusCode());
            healthTracker.recordFailure(HEALTH_KEY, PROVIDER_NAME, "AMFI request failed with HTTP " + ex.getStatusCode().value());
            throw new ProviderException("AMFI NAV file request failed: HTTP " + ex.getStatusCode().value(), ex, retryable);
        } catch (ResourceAccessException ex) {
            log.log(Level.WARNING, "provider=AMFI operation=FETCH_NAV_ALL status=FAILED errorCode=TIMEOUT_OR_UNREACHABLE", ex);
            healthTracker.recordFailure(HEALTH_KEY, PROVIDER_NAME, "AMFI endpoint unreachable or timed out");
            throw new ProviderException("AMFI NAV file endpoint unreachable or timed out", ex, true);
        }
    }

    private ProviderFundData toFundData(AmfiSchemeRecord record) {
        String category = extractCategory(record.schemeCategoryLine());
        String subCategory = extractSubCategory(record.schemeCategoryLine());
        AssetClass assetClass = inferAssetClass(record.schemeCategoryLine());
        PlanType planType = inferPlanType(record.schemeName());
        OptionType optionType = inferOptionType(record.schemeName());
        String amcCode = slugifyAmcCode(record.amcName());

        return new ProviderFundData(
                record.schemeCode(),
                record.isin(),
                amcCode,
                record.amcName(),
                record.schemeName(),
                record.schemeName(),
                category,
                subCategory,
                planType.name(),
                optionType.name(),
                assetClass.name(),
                null, // AMFI does not publish an investment objective
                FundRiskLevel.NOT_RATED.name(), // AMFI does not publish a SEBI riskometer classification
                null, // benchmark - not published by AMFI
                null, // expenseRatio - not published by AMFI
                null, // exitLoad - not published by AMFI
                null, // minimumLumpsum - not published by AMFI
                null, // minimumSip - not published by AMFI
                null, // aum - not published in the daily NAV file
                record.nav(),
                record.navDate(),
                null, // inceptionDate - not published by AMFI
                null  // fundManager - not published by AMFI
        );
    }

    private String extractCategory(String categoryLine) {
        if (categoryLine == null) {
            return "Uncategorized";
        }
        int parenIndex = categoryLine.indexOf('(');
        String prefix = parenIndex > 0 ? categoryLine.substring(0, parenIndex).strip() : categoryLine.strip();
        return prefix.isEmpty() ? "Uncategorized" : prefix;
    }

    private String extractSubCategory(String categoryLine) {
        if (categoryLine == null) {
            return null;
        }
        int start = categoryLine.indexOf('(');
        int end = categoryLine.lastIndexOf(')');
        if (start < 0 || end <= start) {
            return null;
        }
        return categoryLine.substring(start + 1, end).strip();
    }

    private AssetClass inferAssetClass(String categoryLine) {
        if (categoryLine == null) {
            return AssetClass.OTHER;
        }
        String lower = categoryLine.toLowerCase(Locale.ROOT);
        if (lower.contains("equity")) {
            return AssetClass.EQUITY;
        }
        if (lower.contains("debt") || lower.contains("income") || lower.contains("liquid") || lower.contains("gilt")) {
            return AssetClass.DEBT;
        }
        if (lower.contains("hybrid")) {
            return AssetClass.HYBRID;
        }
        if (lower.contains("gold") || lower.contains("silver") || lower.contains("commodit")) {
            return AssetClass.COMMODITY;
        }
        return AssetClass.OTHER;
    }

    /** Best-effort inference from the scheme name text; AMFI does not publish plan type as a separate field. */
    private PlanType inferPlanType(String schemeName) {
        String lower = schemeName.toLowerCase(Locale.ROOT);
        return lower.contains("direct") ? PlanType.DIRECT : PlanType.REGULAR;
    }

    /** Best-effort inference from the scheme name text; AMFI does not publish option type as a separate field. */
    private OptionType inferOptionType(String schemeName) {
        String lower = schemeName.toLowerCase(Locale.ROOT);
        return (lower.contains("idcw") || lower.contains("dividend")) ? OptionType.IDCW : OptionType.GROWTH;
    }

    /** Deterministic, stable code derived from the AMC's official name so repeated syncs map to the same AMC row. */
    private String slugifyAmcCode(String amcName) {
        String slug = amcName.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
        return slug.length() > 50 ? slug.substring(0, 50) : slug;
    }
}
