package com.example.ipcolocationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the IP colocation data returned by the external API.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpInfoResponse {
    private String ipAddress;
    private String continent;
    private String country;
    private String region;
    private String city;
    private double latitude;
    private double longitude;
}
