package com.example.ipcolocationservice.service;

import com.example.ipcolocationservice.dto.IpInfoResponse;
import com.example.ipcolocationservice.exceptions.ExternalApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Service
public class IpLookupService {

    private static final Duration CACHE_TTL = Duration.ofDays(30);
    private static final Duration API_TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public IpLookupService(WebClient freeIpApiWebClient) {
        this.webClient = freeIpApiWebClient;
    }

    public IpInfoResponse lookup(String ip) {
        CacheEntry entry = cache.get(ip);
        if (entry != null && Instant.now().minus(CACHE_TTL).isBefore(entry.timestamp)) {
            return entry.info;
        }

        IpInfoResponse info;
        try {
            info = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{ip}").build(ip))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp ->
                            Mono.error(new IllegalArgumentException("Invalid IP or bad request: " + ip)))
                    .onStatus(HttpStatusCode::is5xxServerError, resp ->
                            Mono.error(new ExternalApiException("FreeIPAPI server error: " + resp.statusCode())))
                    .bodyToMono(IpInfoResponse.class)
                    .timeout(API_TIMEOUT)
                    .block();
        } catch (WebClientResponseException e) {
            throw new ExternalApiException("FreeIPAPI returned error: " + e.getStatusCode(), e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TimeoutException) {
                throw new ExternalApiException("Timeout after " + API_TIMEOUT.getSeconds() + "s calling FreeIPAPI", e);
            }
            throw e;
        }

        if (info == null) {
            throw new ExternalApiException("Empty response from FreeIPAPI for IP: " + ip);
        }

        cache.put(ip, new CacheEntry(info, Instant.now()));
        return info;
    }

    private record CacheEntry(IpInfoResponse info, Instant timestamp) {
    }
}
