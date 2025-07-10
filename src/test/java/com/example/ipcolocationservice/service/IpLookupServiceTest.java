package com.example.ipcolocationservice.service;

import com.example.ipcolocationservice.dto.IpInfoResponse;
import com.example.ipcolocationservice.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpLookupServiceTest {

    private IpGeolocationProvider provider;
    private IpLookupService service;

    @BeforeEach
    void setUp() {
        provider = mock(IpGeolocationProvider.class);
        service = new IpLookupService(provider);
    }

    @Test
    void lookup_successfulResponse_returnsDtoAndCaches() {
        IpInfoResponse mockResponse = new IpInfoResponse(
                "1.2.3.4", "Europe", "Testland", "TestRegion", "TestCity", 10.0, 20.0
        );
        when(provider.getIpInfo("1.2.3.4")).thenReturn(mockResponse);

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

        // Provider should be called only once due to caching
        verify(provider, times(1)).getIpInfo("1.2.3.4");
    }

    @Test
    void lookup_providerThrowsIllegalArgumentException_propagatesException() {
        when(provider.getIpInfo("1.2.3.4"))
                .thenThrow(new IllegalArgumentException("Invalid IP or bad request: 1.2.3.4"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.lookup("1.2.3.4")
        );
        assertTrue(ex.getMessage().contains("Invalid IP or bad request"));
        verify(provider, times(1)).getIpInfo("1.2.3.4");
    }

    @Test
    void lookup_providerThrowsExternalApiException_propagatesException() {
        when(provider.getIpInfo("1.2.3.4"))
                .thenThrow(new ExternalApiException("FreeIPAPI server error: 500"));

        ExternalApiException ex = assertThrows(
                ExternalApiException.class,
                () -> service.lookup("1.2.3.4")
        );
        assertTrue(ex.getMessage().contains("FreeIPAPI server error"));
        verify(provider, times(1)).getIpInfo("1.2.3.4");
    }
}
