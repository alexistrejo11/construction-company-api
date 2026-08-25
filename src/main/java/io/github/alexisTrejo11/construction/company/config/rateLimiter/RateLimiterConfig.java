package io.github.alexisTrejo11.construction.company.config.rateLimiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public Bucket bucket(
        @Value("${app.rate-limit.capacity:20}") long capacity,
        @Value("${app.rate-limit.refill-tokens:10}") long refillTokens,
        @Value("${app.rate-limit.refill-minutes:1}") long refillMinutes
    ) {
        Refill refill = Refill.intervally(refillTokens, Duration.ofMinutes(refillMinutes));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
