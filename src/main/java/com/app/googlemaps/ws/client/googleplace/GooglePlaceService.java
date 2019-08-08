package com.app.googlemaps.ws.client.googleplace;

import com.app.googlemaps.api.NearBySearchResponse;
import com.app.googlemaps.api.PlaceDetail;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface GooglePlaceService {
    Mono<NearBySearchResponse> getNearByPlaces(String loc, String type, String radius);

    Mono<PlaceDetail> getPlaceDetail(String placeId);
}
