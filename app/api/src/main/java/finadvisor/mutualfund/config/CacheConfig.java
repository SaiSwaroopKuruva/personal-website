package finadvisor.mutualfund.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caching abstraction for infrequently-changing mutual fund data (filter metadata, popular funds, fund
 * details, current NAV - Part 30). Backed by an in-memory {@link ConcurrentMapCacheManager} today; swap
 * this single bean for a {@code RedisCacheManager} when Redis is introduced - no service code changes
 * needed since callers only use the {@code @Cacheable}/{@code @CacheEvict} abstraction.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String FUND_FILTERS_CACHE = "mutualFundFilters";
    public static final String FUND_DETAILS_CACHE = "mutualFundDetails";
    public static final String POPULAR_FUNDS_CACHE = "popularMutualFunds";
    public static final String CURRENT_NAV_CACHE = "mutualFundCurrentNav";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(FUND_FILTERS_CACHE, FUND_DETAILS_CACHE, POPULAR_FUNDS_CACHE, CURRENT_NAV_CACHE);
    }
}
