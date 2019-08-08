package com.app.googlemaps.config;

import lombok.Data;

import java.util.List;

@Data
public class GoogleMapProperties extends WSProperties {
    private String nearBySearchUri;
    private String placeDetailsUri;
    private List<String> keys;
}
