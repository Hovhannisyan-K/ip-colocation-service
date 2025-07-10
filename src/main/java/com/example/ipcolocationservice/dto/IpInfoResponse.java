package com.example.ipcolocationservice.dto;

import lombok.Data;

/**
 * Represents the IP colocation data returned by the external API.
 */
@Data
public class IpInfoResponse {
    private String ipAddress;
    private String continent;
    private String country;
    private String region;
    private String city;
    private double latitude;
    private double longitude;
}
