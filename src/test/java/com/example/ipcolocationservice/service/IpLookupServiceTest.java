package com.example.ipcolocationservice.service;

import com.example.ipcolocationservice.dto.IpInfoResponse;
import com.example.ipcolocationservice.exceptions.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IpLookupServiceTest {

    private ExchangeFunction exchangeFunction;
    private IpLookupService service;

    @BeforeEach
    void setUp() {
        exchangeFunction = mock(ExchangeFunction.class);
        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .baseUrl("https://freeipapi.com/api/json")
                .build();
        service = new IpLookupService(webClient);
    }

    @Test
    void lookup_successfulResponse_returnsDtoAndCaches() {
        String json = """
                {
                  "ipAddress":"1.2.3.4",
                  "continent":"Europe",
                  "country":"Testland",
                  "region":"TestRegion",
                  "city":"TestCity",
                  "latitude":10.0,
                  "longitude":20.0
                }
                """;
        ClientResponse okResponse = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(okResponse));

        IpInfoResponse first = service.lookup("1.2.3.4");
        IpInfoResponse second = service.lookup("1.2.3.4");

        assertEquals("1.2.3.4", first.getIpAddress());
        assertEquals("Europe", first.getContinent());
        assertEquals("Testland", first.getCountry());
        assertEquals("TestRegion", first.getRegion());
        assertEquals("TestCity", first.getCity());
        assertEquals(10.0, first.getLatitude());
        assertEquals(20.0, first.getLongitude());

        assertSame(first, second);

        verify(exchangeFunction, times(1)).exchange(any());
    }

    @Test
    void lookup_clientError_throwsIllegalArgumentException() {
        ClientResponse badRequest = ClientResponse.create(HttpStatus.BAD_REQUEST).build();
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(badRequest));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.lookup("1.2.3.4")
        );
        assertTrue(ex.getMessage().contains("Invalid IP or bad request"));
    }

    @Test
    void lookup_serverError_throwsExternalApiException() {
        ClientResponse serverError = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(serverError));
        ExternalApiException ex = assertThrows(
                ExternalApiException.class,
                () -> service.lookup("1.2.3.4")
        );
        assertTrue(ex.getMessage().contains("FreeIPAPI server error"));
    }
}
