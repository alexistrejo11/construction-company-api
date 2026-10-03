package io.github.alexisTrejo11.construction.company.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "adminDashboardCache",
                "adminSettingsCache",
                "userCredentialsCache"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(1, TimeUnit.HOURS)
        );

        cacheManager.setAllowNullValues(false);
        cacheManager.setAsyncCacheMode(true);

        return cacheManager;
    }
}