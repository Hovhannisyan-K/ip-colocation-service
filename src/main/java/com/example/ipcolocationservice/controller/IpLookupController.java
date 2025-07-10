package com.example.ipcolocationservice.controller;

import com.example.ipcolocationservice.dto.IpInfoResponse;
import com.example.ipcolocationservice.exceptions.ExternalApiException;
import com.example.ipcolocationservice.service.IpLookupService;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/ip")
public class IpLookupController {

    private final IpLookupService lookupService;

    public IpLookupController(IpLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/{ip}")
    public IpInfoResponse getIpInfo(@PathVariable String ip) {
        if (!isValidIp(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP: " + ip);
        }
        try {
            return lookupService.lookup(ip);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ExternalApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }


    private boolean isValidIp(String ip) {
        return InetAddressValidator.getInstance().isValid(ip);
    }
}
