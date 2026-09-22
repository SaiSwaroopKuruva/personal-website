package finadvisor.stock.provider.upstox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finadvisor.marketdata.ErrorCategory;
import finadvisor.marketdata.ProviderHealthTracker;
import finadvisor.stock.provider.StockProviderException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thin HTTP wrapper around the Upstox REST API. Centralizes auth header attachment, structured
 * success/failure logging (Part 14 - never logs the Authorization header/token value), and HTTP-status-to
 * -{@link ErrorCategory} mapping (Part 15) used by every {@link finadvisor.stock.provider.StockMarketDataProvider}
 * operation.
 */
@Component
public class UpstoxApiClient {

    private static final Logger log = Logger.getLogger(UpstoxApiClient.class.getName());
    private static final String PROVIDER_NAME = "UPSTOX";
    static final String HEALTH_KEY = "stocks";

    private final RestClient restClient;
    private final UpstoxProperties properties;
    private final ObjectMapper objectMapper;
    private final ProviderHealthTracker healthTracker;

    public UpstoxApiClient(RestClient.Builder restClientBuilder, UpstoxProperties properties,
                            ObjectMapper objectMapper, ProviderHealthTracker healthTracker) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.healthTracker = healthTracker;
        this.restClient = restClientBuilder.baseUrl(properties.getApiBaseUrl()).build();
    }

    /** Performs a GET request and returns the parsed JSON body's top-level {@code data} node. */
    public JsonNode getData(String operation, String uri) {
        return getData(operation, b -> URI.create(properties.getApiBaseUrl() + uri));
    }

    /** Same as {@link #getData(String, String)} but lets the caller build the URI via {@link UriBuilder} (needed
     * when path segments contain characters like '|' that must be percent-encoded, e.g. instrument keys). */
    public JsonNode getData(String operation, Function<UriBuilder, URI> uriFunction) {
        if (properties.getAnalyticsToken() == null || properties.getAnalyticsToken().isBlank()) {
            throw new StockProviderException("UPSTOX_ANALYTICS_TOKEN is not configured", ErrorCategory.AUTHENTICATION_ERROR);
        }
        try {
            String body = restClient.get()
                    .uri(uriFunction)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAnalyticsToken())
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(body);
            log.info(() -> "provider=UPSTOX operation=" + operation + " status=SUCCESS");
            healthTracker.recordSuccess(HEALTH_KEY, PROVIDER_NAME, null);
            JsonNode data = root.path("data");
            if (data.isMissingNode()) {
                throw new StockProviderException("Upstox response for " + operation + " had no 'data' field", ErrorCategory.INVALID_PROVIDER_RESPONSE);
            }
            return data;
        } catch (RestClientResponseException ex) {
            ErrorCategory category = mapHttpStatus(ex.getStatusCode());
            log.log(Level.WARNING, () -> "provider=UPSTOX operation=" + operation + " status=FAILED httpStatus=" + ex.getStatusCode().value()
                    + " errorCode=" + category);
            healthTracker.recordFailure(HEALTH_KEY, PROVIDER_NAME, operation + " failed with HTTP " + ex.getStatusCode().value());
            throw new StockProviderException("Upstox " + operation + " request failed: HTTP " + ex.getStatusCode().value(), category, ex);
        } catch (ResourceAccessException ex) {
            log.log(Level.WARNING, "provider=UPSTOX operation=" + operation + " status=FAILED errorCode=TIMEOUT", ex);
            healthTracker.recordFailure(HEALTH_KEY, PROVIDER_NAME, operation + " timed out or endpoint unreachable");
            throw new StockProviderException("Upstox " + operation + " timed out or was unreachable", ErrorCategory.TIMEOUT, ex);
        } catch (StockProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            log.log(Level.WARNING, "provider=UPSTOX operation=" + operation + " status=FAILED errorCode=INVALID_PROVIDER_RESPONSE", ex);
            healthTracker.recordFailure(HEALTH_KEY, PROVIDER_NAME, operation + " returned a malformed response");
            throw new StockProviderException("Upstox " + operation + " returned a malformed response", ErrorCategory.INVALID_PROVIDER_RESPONSE, ex);
        }
    }

    private ErrorCategory mapHttpStatus(HttpStatusCode status) {
        int value = status.value();
        if (value == 401) {
            return ErrorCategory.AUTHENTICATION_ERROR;
        }
        if (value == 403) {
            return ErrorCategory.AUTHORIZATION_ERROR;
        }
        if (value == 429) {
            return ErrorCategory.RATE_LIMITED;
        }
        if (value == 400 || value == 404 || value == 406) {
            return ErrorCategory.INVALID_REQUEST;
        }
        if (status.is5xxServerError()) {
            return ErrorCategory.PROVIDER_UNAVAILABLE;
        }
        return ErrorCategory.UNKNOWN;
    }
}
