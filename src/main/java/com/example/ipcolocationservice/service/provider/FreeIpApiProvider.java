package com.example.ipcolocationservice.service.provider;

import com.example.ipcolocationservice.dto.IpInfoResponse;
import com.example.ipcolocationservice.exception.ExternalApiException;
import com.example.ipcolocationservice.service.IpGeolocationProvider;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class FreeIpApiProvider implements IpGeolocationProvider {

    private static final Duration API_TIMEOUT = Duration.ofSeconds(5);
    private final WebClient webClient;

    public FreeIpApiProvider(WebClient freeIpApiWebClient) {
        this.webClient = freeIpApiWebClient;
    }

    @Override
    public IpInfoResponse getIpInfo(String ip) {
        IpInfoResponse info;
        try {
            info = webClient.get()
                    .uri(u -> u.path("/{ip}").build(ip))
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

        return info;
    }

    @Override
    public String getProviderName() {
        return "FreeIPAPI";
    }
}
