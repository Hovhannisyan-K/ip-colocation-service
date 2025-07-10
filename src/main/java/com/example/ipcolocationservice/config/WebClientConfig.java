package com.example.ipcolocationservice.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${freeipapi.baseurl}")
    private String baseUrl;

    private static final int PERMITS_PER_PERIOD = 1;
    private static final Duration REFRESH_PERIOD = Duration.ofSeconds(1);
    private static final Duration WAIT_DURATION = Duration.ZERO;

    @Bean
    public RateLimiter freeIpApiRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(PERMITS_PER_PERIOD)
                .limitRefreshPeriod(REFRESH_PERIOD)
                .timeoutDuration(WAIT_DURATION)
                .build();

        return RateLimiterRegistry.of(config)
                .rateLimiter("freeIpApi");
    }

    @Bean
    public WebClient freeIpApiWebClient(RateLimiter freeIpApiRateLimiter) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter((request, next) ->
                        next.exchange(request)
                                .transformDeferred(RateLimiterOperator.of(freeIpApiRateLimiter))
                )
                .build();
    }
}
