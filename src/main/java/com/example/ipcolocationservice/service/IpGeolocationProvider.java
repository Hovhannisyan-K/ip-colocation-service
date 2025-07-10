package com.example.ipcolocationservice.service;

import com.example.ipcolocationservice.dto.IpInfoResponse;

public interface IpGeolocationProvider {
    IpInfoResponse getIpInfo(String ip);

    String getProviderName();
}
