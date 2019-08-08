package com.app.googlemaps.service;

import com.app.googlemaps.api.PlaceDetail;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface GoogleMapService {
    Flux<PlaceDetail.Result> getNearByPlaces(String lat, String lng, String type, String radius);
}
