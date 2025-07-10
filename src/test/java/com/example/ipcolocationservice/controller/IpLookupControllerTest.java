package com.example.ipcolocationservice.controller;

import com.example.ipcolocationservice.dto.IpInfoResponse;
import com.example.ipcolocationservice.exceptions.ExternalApiException;
import com.example.ipcolocationservice.service.IpLookupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IpLookupController.class)
class IpLookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IpLookupService lookupService;

    @Test
    @DisplayName("GET /ip/{ip} – valid IP returns 200 and body")
    void getIpInfo_validIp_returnsInfo() throws Exception {
        IpInfoResponse resp = new IpInfoResponse(
                "136.159.0.0", "Americas", "Canada", "Alberta", "Calgary",
                51.075153, -114.12841
        );
        when(lookupService.lookup("136.159.0.0")).thenReturn(resp);

        mockMvc.perform(get("/ip/136.159.0.0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ipAddress").value("136.159.0.0"))
                .andExpect(jsonPath("$.continent").value("Americas"))
                .andExpect(jsonPath("$.country").value("Canada"))
                .andExpect(jsonPath("$.region").value("Alberta"))
                .andExpect(jsonPath("$.city").value("Calgary"))
                .andExpect(jsonPath("$.latitude").value(51.075153))
                .andExpect(jsonPath("$.longitude").value(-114.12841));

        verify(lookupService).lookup("136.159.0.0");
    }

    @Test
    @DisplayName("GET /ip/{ip} – invalid IP format returns 400")
    void getIpInfo_invalidIp_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/ip/not-an-ip"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid IP address: not-an-ip"));

        verifyNoInteractions(lookupService);
    }

    @Test
    @DisplayName("GET /ip/{ip} – service throws IllegalArgumentException → 400")
    void getIpInfo_serviceThrowsIllegalArg_returnsBadRequest() throws Exception {
        when(lookupService.lookup("1.2.3.4"))
                .thenThrow(new IllegalArgumentException("Bad IP lookup"));

        mockMvc.perform(get("/ip/1.2.3.4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad IP lookup"));

        verify(lookupService).lookup("1.2.3.4");
    }

    @Test
    @DisplayName("GET /ip/{ip} – service throws ExternalApiException → 502")
    void getIpInfo_serviceThrowsExternalApi_returnsBadGateway() throws Exception {
        when(lookupService.lookup("5.6.7.8"))
                .thenThrow(new ExternalApiException("Upstream error"));

        mockMvc.perform(get("/ip/5.6.7.8"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Upstream error"));

        verify(lookupService).lookup("5.6.7.8");
    }

    @Test
    @DisplayName("GET /ip/{ip} – service throws RuntimeException → 500")
    void getIpInfo_serviceThrowsRuntime_returnsServerError() throws Exception {
        when(lookupService.lookup("9.9.9.9"))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(get("/ip/9.9.9.9"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Unexpected error: Unexpected"));

        verify(lookupService).lookup("9.9.9.9");
    }
}
