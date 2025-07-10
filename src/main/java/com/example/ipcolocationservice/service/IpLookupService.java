package com.example.ipcolocationservice.service;

import com.example.ipcolocationservice.dto.IpInfoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IpLookupService {

    private static final Duration CACHE_TTL = Duration.ofDays(30);

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

        IpInfoResponse info = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{ip}")
                        .build(ip))
                .retrieve()
                .bodyToMono(IpInfoResponse.class)
                .block();

        cache.put(ip, new CacheEntry(info, Instant.now()));
        return info;
    }

    private record CacheEntry(IpInfoResponse info, Instant timestamp) {
    }
}
