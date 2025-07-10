package com.example.ipcolocationservice.controller;

import com.example.ipcolocationservice.dto.IpInfoResponse;
import com.example.ipcolocationservice.exception.ExternalApiException;
import com.example.ipcolocationservice.service.IpLookupService;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ip")
public class IpLookupController {

    private final IpLookupService lookupService;

    public IpLookupController(IpLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/{ip}")
    public ResponseEntity<?> getIpInfo(@PathVariable String ip) {
        if (!isValidIp(ip)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Invalid IP address: " + ip));
        }

        try {
            IpInfoResponse info = lookupService.lookup(ip);
            return ResponseEntity.ok(info);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (ExternalApiException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }


    private boolean isValidIp(String ip) {
        return InetAddressValidator.getInstance().isValid(ip);
    }
}
