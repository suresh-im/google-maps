package com.app.googlemaps.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "properties")
public class AppConfigProperties {
    private GoogleMapProperties googleMapProperties;
}