package com.example.ipcolocationservice.controller;

import com.example.ipcolocationservice.service.IpLookupService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ip")
public class IpLookupController {

    private final IpLookupService lookupService;

    public IpLookupController(IpLookupService lookupService) {
        this.lookupService = lookupService;
    }


}
