package com.jFastApi.security;

import com.jFastApi.annotation.Bean;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import java.time.Duration;

@Bean
public class ApiRateLimiter {

    private final RateLimiterRegistry rateLimiterRegistry;

    public ApiRateLimiter() {
        this.rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
    }

    public boolean canLoad(String uniqueKey, int callNumber, Duration timeoutDuration) {

        if (!SecurityContext.isRateLimitEnabled()) {
            return true;
        }

        // Refresh period should match the timeout because after the timeoutDuration ends we want it to refresh.
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(callNumber)
                .limitRefreshPeriod(timeoutDuration)
                .timeoutDuration(Duration.ofMillis(1))
                .build();

        try {
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(uniqueKey, config);
            return rateLimiter.acquirePermission();

        } catch (RequestNotPermitted exception) {
            return false;
        }
    }
}