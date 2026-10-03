package io.github.alexisTrejo11.construction.company.config.rateLimiter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final Bucket bucket;

    public RateLimitInterceptor(Bucket bucket) {
        this.bucket = bucket;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            String responseBody = "{ \"error\": \"You have exhausted your API Request Quota\", " +
                    "\"retry_after_seconds\": \"" + waitForRefill + "\" }";
            response.getWriter().write(responseBody);
            return false;
        }
    }
}
