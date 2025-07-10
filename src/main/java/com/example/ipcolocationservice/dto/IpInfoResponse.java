package com.example.ipcolocationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpInfoResponse {
    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("continent")
    private String continent;

    @JsonProperty("countryName")
    private String country;

    @JsonProperty("regionName")
    private String region;

    @JsonProperty("cityName")
    private String city;

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;
}
